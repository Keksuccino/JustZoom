package de.keksuccino.justzoom.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A standalone, typed JSON store for configuration options and persistent data.
 * Values can live at the document root or inside named sections, and changes are saved immediately.
 * Saving re-reads the file and only writes the values this instance changed, so several instances can share one file and edits made to the file while the game runs are never reverted.
 */
public class JsonConfig {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // Serializes the read-merge-write cycle of all instances, so two instances sharing a file cannot lose each other's changes.
    private static final Object FILE_WRITE_LOCK = new Object();

    private final Path file;
    @Nullable
    private final Path legacyFile;
    private final Map<ValuePath, ConfigValue<?>> registeredValues = new LinkedHashMap<>();
    private JsonObject values = new JsonObject();
    // Values this instance changed since the last successful save. A null value marks a removed value.
    private final Map<ValuePath, JsonElement> pendingChanges = new LinkedHashMap<>();
    private boolean dirty;
    private boolean migrationBlocked;
    private boolean migrationPending;

    public JsonConfig(@NotNull File file) {
        this(Objects.requireNonNull(file).toPath(), null);
    }

    public JsonConfig(@NotNull File file, @Nullable File legacyFile) {
        this(Objects.requireNonNull(file).toPath(), legacyFile != null ? legacyFile.toPath() : null);
    }

    public JsonConfig(@NotNull Path file) {
        this(file, null);
    }

    public JsonConfig(@NotNull Path file, @Nullable Path legacyFile) {
        this.file = normalize(file);
        this.legacyFile = legacyFile != null ? normalize(legacyFile) : null;
        if (this.file.equals(this.legacyFile)) throw new IllegalArgumentException("The JSON and legacy config paths must be different.");
        this.loadInitialValues();
    }

    /**
     * Creates a handle for a named section. Calling this method does not modify the document by itself.
     */
    @NotNull
    public ConfigSection section(@NotNull String name) {
        return new ConfigSection(this, requireName(name, "section"));
    }

    /**
     * Defines a root value and infers its stored type from its default value.
     */
    @NotNull
    public <T> ConfigValue<T> option(@NotNull String key, @NotNull T defaultValue) {
        return this.option(null, key, defaultValue);
    }

    /**
     * Defines a root value with an explicit type.
     */
    @NotNull
    public <T> ConfigValue<T> option(@NotNull String key, @NotNull Type type, @NotNull T defaultValue) {
        return this.option(null, key, type, defaultValue);
    }

    /**
     * Defines an optional root value that is absent until explicitly set.
     */
    @NotNull
    public <T> ConfigValue<T> optional(@NotNull String key, @NotNull Class<T> type) {
        return this.optional(null, key, type);
    }

    /**
     * Defines an optional root value with a parameterized or otherwise non-Class type.
     */
    @NotNull
    public <T> ConfigValue<T> optional(@NotNull String key, @NotNull Type type) {
        return this.optional(null, key, type);
    }

    @NotNull
    <T> ConfigValue<T> option(@Nullable String section, @NotNull String key, @NotNull T defaultValue) {
        Objects.requireNonNull(defaultValue);
        return this.option(section, key, defaultValue.getClass(), defaultValue);
    }

    @NotNull
    <T> ConfigValue<T> option(@Nullable String section, @NotNull String key, @NotNull Type type, @NotNull T defaultValue) {
        return this.define(section, key, type, Objects.requireNonNull(defaultValue), true);
    }

    @NotNull
    <T> ConfigValue<T> optional(@Nullable String section, @NotNull String key, @NotNull Type type) {
        return this.define(section, key, type, null, false);
    }

    /**
     * Writes all pending changes. This is useful after declaring several defaults because individual declarations are batched in memory.
     */
    public final synchronized boolean save() {
        if (this.migrationBlocked) {
            LOGGER.error("Refusing to write config {} because its legacy source {} could not be read.", this.file, this.legacyFile);
            return false;
        }
        if (!this.dirty && Files.exists(this.file)) {
            this.finishMigration();
            return true;
        }
        if (!this.writeDocument()) return false;
        this.dirty = false;
        this.pendingChanges.clear();
        this.finishMigration();
        return true;
    }

    /**
     * Reloads the document and reapplies declared defaults for missing or invalid values.
     */
    public final synchronized boolean reload() {
        this.values = new JsonObject();
        this.pendingChanges.clear();
        this.dirty = false;
        this.migrationBlocked = false;
        this.migrationPending = false;
        this.loadInitialValues();
        if (this.migrationBlocked) return false;
        for (ConfigValue<?> value : this.registeredValues.values()) {
            this.restoreDefaultIfNeeded(value);
        }
        return !this.dirty || this.save();
    }

    @NotNull
    public Path getFile() {
        return this.file;
    }

    @Nullable
    public Path getLegacyFile() {
        return this.legacyFile;
    }

    synchronized <T> void setValue(@NotNull ConfigValue<T> value, @NotNull T newValue) {
        JsonElement serializedValue = this.serialize(newValue, value.getType(), value.getPath());
        JsonElement previousValue = getElement(this.values, value.getPath());
        T previousStoredValue = value.getStoredValue();
        boolean wasDirty = this.dirty;
        if (serializedValue.equals(previousValue)) {
            if (this.dirty || Files.notExists(this.file)) this.save();
            return;
        }

        boolean hadPendingChange = this.pendingChanges.containsKey(value.getPath());
        JsonElement previousPendingChange = this.pendingChanges.get(value.getPath());
        this.changeElement(value.getPath(), serializedValue);
        value.setStoredValue(newValue);
        if (!this.save()) {
            this.restoreElement(value.getPath(), previousValue);
            this.restorePendingChange(value.getPath(), hadPendingChange, previousPendingChange);
            value.setStoredValue(previousStoredValue);
            this.dirty = wasDirty;
        }
    }

    synchronized <T> void updateValue(@NotNull ConfigValue<T> value, @NotNull UnaryOperator<T> updater) {
        this.setValue(value, Objects.requireNonNull(updater.apply(value.getValue())));
    }

    synchronized void removeValue(@NotNull ConfigValue<?> value) {
        JsonElement previousValue = getElement(this.values, value.getPath());
        if (previousValue == null) return;
        Object previousStoredValue = value.getStoredValue();
        boolean wasDirty = this.dirty;
        boolean hadPendingChange = this.pendingChanges.containsKey(value.getPath());
        JsonElement previousPendingChange = this.pendingChanges.get(value.getPath());
        this.changeElement(value.getPath(), null);
        value.setStoredValue(null);
        if (!this.save()) {
            this.restoreElement(value.getPath(), previousValue);
            this.restorePendingChange(value.getPath(), hadPendingChange, previousPendingChange);
            this.restoreStoredValue(value, previousStoredValue);
            this.dirty = wasDirty;
        }
    }

    @NotNull
    private synchronized <T> ConfigValue<T> define(@Nullable String section, @NotNull String key, @NotNull Type type, @Nullable T defaultValue, boolean hasDefaultValue) {
        String validatedSection = section != null ? requireName(section, "section") : null;
        ValuePath path = new ValuePath(validatedSection, requireName(key, "key"));
        Objects.requireNonNull(type);
        if (this.registeredValues.containsKey(path)) throw new IllegalArgumentException("Config value '" + path + "' is already defined.");

        ConfigValue<T> value = new ConfigValue<>(this, path, type, defaultValue, hasDefaultValue);
        if (hasDefaultValue) {
            this.serialize(Objects.requireNonNull(defaultValue), type, path);
        }
        this.registeredValues.put(path, value);
        this.restoreDefaultIfNeeded(value);
        return value;
    }

    private <T> void restoreDefaultIfNeeded(@NotNull ConfigValue<T> value) {
        JsonElement storedValue = getElement(this.values, value.getPath());
        T deserializedValue = storedValue != null && !storedValue.isJsonNull() ? this.deserialize(storedValue, value.getType(), value.getPath()) : null;
        if (deserializedValue != null) {
            value.setStoredValue(deserializedValue);
            return;
        }
        value.setStoredValue(null);
        if (!value.hasDefaultValue()) return;
        T defaultValue = value.getDefaultValue();
        this.changeElement(value.getPath(), this.serialize(defaultValue, value.getType(), value.getPath()));
        value.setStoredValue(defaultValue);
    }

    @SuppressWarnings("unchecked")
    private <T> void restoreStoredValue(@NotNull ConfigValue<T> value, @Nullable Object storedValue) {
        value.setStoredValue((T) storedValue);
    }

    private void loadInitialValues() {
        if (Files.exists(this.file)) {
            this.values = this.readDocument();
            return;
        }
        if (this.legacyFile == null || Files.notExists(this.legacyFile)) return;

        this.migrationPending = true;
        try {
            LegacyConfigReader.Result result = LegacyConfigReader.read(this.legacyFile);
            this.values = result.getValues();
            this.dirty = true;
            if (!result.isComplete()) {
                this.migrationBlocked = true;
                LOGGER.error("Legacy config {} contains unrecognized data. Its parsed values will be used in memory, but no files will be replaced.", this.legacyFile);
                return;
            }
        } catch (IOException | RuntimeException ex) {
            this.migrationBlocked = true;
            LOGGER.error("Failed to read legacy config {}. The replacement config will not be written until the legacy file can be read.", this.legacyFile, ex);
            return;
        }

        this.save();
    }

    @NotNull
    private JsonObject readDocument() {
        JsonObject document = this.readDocumentOrNull();
        return document != null ? document : new JsonObject();
    }

    @Nullable
    private JsonObject readDocumentOrNull() {
        try (Reader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root != null && root.isJsonObject()) return root.getAsJsonObject();
            LOGGER.warn("Ignoring config {} because its JSON root is not an object.", this.file);
        } catch (IOException | RuntimeException ex) {
            LOGGER.warn("Failed to read config {}. It will be repaired when a value is next saved.", this.file, ex);
        }
        return null;
    }

    private boolean writeDocument() {
        synchronized (FILE_WRITE_LOCK) {
            Path temporaryFile = null;
            try {
                JsonObject document = this.createDocumentToWrite();
                Path parent = this.file.getParent();
                Files.createDirectories(parent);
                String temporaryPrefix = this.file.getFileName().toString();
                // Files.createTempFile requires at least three prefix characters, including for otherwise valid one-character file names.
                if (temporaryPrefix.length() < 3) temporaryPrefix = (temporaryPrefix + "___").substring(0, 3);
                temporaryFile = Files.createTempFile(parent, temporaryPrefix, ".tmp");
                Files.writeString(temporaryFile, GSON.toJson(document) + System.lineSeparator(), StandardCharsets.UTF_8);
                this.replaceWith(temporaryFile);
                return true;
            } catch (IOException | RuntimeException ex) {
                LOGGER.error("Failed to write config {}.", this.file, ex);
                return false;
            } finally {
                if (temporaryFile != null) {
                    try {
                        Files.deleteIfExists(temporaryFile);
                    } catch (IOException ex) {
                        LOGGER.warn("Failed to clean up temporary config file {}.", temporaryFile, ex);
                    }
                }
            }
        }
    }

    /**
     * Applies this instance's pending changes to the current file content instead of writing the in-memory document as a whole.
     * Other values in the file may have been edited by hand or written by another instance since this instance loaded the file.
     * The in-memory document is only written as a whole when the file is missing or unreadable, which also repairs a damaged file.
     */
    @NotNull
    private JsonObject createDocumentToWrite() {
        JsonObject document = Files.exists(this.file) ? this.readDocumentOrNull() : null;
        if (document == null) return this.values.deepCopy();
        for (Map.Entry<ValuePath, JsonElement> change : this.pendingChanges.entrySet()) {
            if (change.getValue() == null) {
                removeElement(document, change.getKey());
            } else {
                putElement(document, change.getKey(), change.getValue().deepCopy());
            }
        }
        return document;
    }

    private void replaceWith(@NotNull Path temporaryFile) throws IOException {
        // Keeping the temporary file beside the target guarantees that an atomic move cannot cross file-system boundaries.
        try {
            Files.move(temporaryFile, this.file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporaryFile, this.file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void finishMigration() {
        if (!this.migrationPending || this.legacyFile == null) return;
        try {
            Files.deleteIfExists(this.legacyFile);
            this.migrationPending = false;
            LOGGER.info("Converted legacy config {} to {}.", this.legacyFile, this.file);
        } catch (IOException ex) {
            LOGGER.warn("Converted legacy config {} but could not remove the old file.", this.legacyFile, ex);
        }
    }

    @Nullable
    private <T> T deserialize(@NotNull JsonElement element, @NotNull Type type, @NotNull ValuePath path) {
        try {
            if (!isCompatibleJsonType(element, type)) throw new JsonParseException("Unexpected JSON value type");
            T value = GSON.fromJson(element, type);
            if (value instanceof Float && !Float.isFinite((Float) value)) throw new JsonParseException("Non-finite float");
            if (value instanceof Double && !Double.isFinite((Double) value)) throw new JsonParseException("Non-finite double");
            return value;
        } catch (RuntimeException ex) {
            LOGGER.warn("Ignoring invalid value '{}' in config {}.", path, this.file, ex);
            return null;
        }
    }

    @NotNull
    private JsonElement serialize(@NotNull Object value, @NotNull Type type, @NotNull ValuePath path) {
        try {
            JsonElement serializedValue = GSON.toJsonTree(value, type);
            if (serializedValue == null || serializedValue.isJsonNull()) throw new IllegalArgumentException("Serialized value is null");
            return serializedValue;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Failed to serialize config value '" + path + "'.", ex);
        }
    }

    /**
     * Changes a value in memory and marks it for the next save. A null value removes it.
     */
    private void changeElement(@NotNull ValuePath path, @Nullable JsonElement value) {
        if (value == null) {
            removeElement(this.values, path);
        } else {
            putElement(this.values, path, value);
        }
        this.pendingChanges.put(path, value);
        this.dirty = true;
    }

    private void restoreElement(@NotNull ValuePath path, @Nullable JsonElement previousValue) {
        if (previousValue == null) {
            removeElement(this.values, path);
        } else {
            putElement(this.values, path, previousValue);
        }
    }

    private void restorePendingChange(@NotNull ValuePath path, boolean hadPendingChange, @Nullable JsonElement previousPendingChange) {
        if (hadPendingChange) {
            this.pendingChanges.put(path, previousPendingChange);
        } else {
            this.pendingChanges.remove(path);
        }
    }

    @Nullable
    private static JsonElement getElement(@NotNull JsonObject document, @NotNull ValuePath path) {
        if (path.section == null) return document.get(path.key);
        JsonElement section = document.get(path.section);
        return section != null && section.isJsonObject() ? section.getAsJsonObject().get(path.key) : null;
    }

    private static void putElement(@NotNull JsonObject document, @NotNull ValuePath path, @NotNull JsonElement value) {
        if (path.section == null) {
            document.add(path.key, value);
            return;
        }
        JsonElement existingSection = document.get(path.section);
        JsonObject section = existingSection != null && existingSection.isJsonObject() ? existingSection.getAsJsonObject() : new JsonObject();
        section.add(path.key, value);
        document.add(path.section, section);
    }

    private static void removeElement(@NotNull JsonObject document, @NotNull ValuePath path) {
        if (path.section == null) {
            document.remove(path.key);
            return;
        }
        JsonElement existingSection = document.get(path.section);
        if (existingSection == null || !existingSection.isJsonObject()) return;
        JsonObject section = existingSection.getAsJsonObject();
        section.remove(path.key);
        if (section.size() == 0) document.remove(path.section);
    }

    private static boolean isCompatibleJsonType(@NotNull JsonElement element, @NotNull Type type) {
        if (!(type instanceof Class<?>)) return true;
        Class<?> valueClass = (Class<?>) type;
        if (valueClass == String.class || valueClass == Character.class || valueClass == char.class || valueClass.isEnum()) return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
        if (valueClass == Boolean.class || valueClass == boolean.class) return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
        if (Number.class.isAssignableFrom(valueClass) || valueClass.isPrimitive()) return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
        if (JsonObject.class.isAssignableFrom(valueClass)) return element.isJsonObject();
        return true;
    }

    @NotNull
    private static Path normalize(@NotNull Path path) {
        return Objects.requireNonNull(path).toAbsolutePath().normalize();
    }

    @NotNull
    private static String requireName(@NotNull String name, @NotNull String kind) {
        String validatedName = Objects.requireNonNull(name).trim();
        if (validatedName.isEmpty()) throw new IllegalArgumentException("Config " + kind + " must not be empty.");
        return validatedName;
    }

    static final class ValuePath {

        @Nullable
        private final String section;
        private final String key;

        private ValuePath(@Nullable String section, @NotNull String key) {
            this.section = section;
            this.key = key;
        }

        @Nullable
        String getSection() {
            return this.section;
        }

        @NotNull
        String getKey() {
            return this.key;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof ValuePath)) return false;
            ValuePath valuePath = (ValuePath) object;
            return Objects.equals(this.section, valuePath.section) && this.key.equals(valuePath.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.section, this.key);
        }

        @Override
        public String toString() {
            return this.section != null ? this.section + "." + this.key : this.key;
        }

    }

}
