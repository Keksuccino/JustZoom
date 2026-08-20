package de.keksuccino.justzoom.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class PersistenceData {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final DataKey<Float> LAST_ZOOM_LEVEL = new DataKey<>("last_zoom_level", Float.class);

    private final Path file;
    private final JsonObject values;

    public PersistenceData(@NotNull File file) {
        this.file = Objects.requireNonNull(file).toPath().toAbsolutePath().normalize();
        this.values = this.read();
    }

    @NotNull
    public synchronized <T> T get(@NotNull DataKey<T> key, @NotNull T fallback) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(fallback);
        JsonElement storedValue = this.values.get(key.name);
        if (storedValue == null || storedValue.isJsonNull()) return fallback;

        try {
            T value = GSON.fromJson(storedValue, key.type);
            return value != null ? value : fallback;
        } catch (RuntimeException ex) {
            LOGGER.warn("[JUST ZOOM] Ignoring invalid persistence value '{}' in {}.", key.name, this.file, ex);
            return fallback;
        }
    }

    public synchronized <T> void set(@NotNull DataKey<T> key, @NotNull T value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        JsonElement serializedValue;
        try {
            serializedValue = GSON.toJsonTree(value, key.type);
        } catch (RuntimeException ex) {
            LOGGER.error("[JUST ZOOM] Failed to serialize persistence value '{}'.", key.name, ex);
            return;
        }

        JsonElement previousValue = this.values.get(key.name);
        if (serializedValue.equals(previousValue)) return;

        this.values.add(key.name, serializedValue);
        if (!this.write()) {
            // Keep the in-memory document consistent with the last successful disk state so a later setter call retries the write.
            if (previousValue == null) {
                this.values.remove(key.name);
            } else {
                this.values.add(key.name, previousValue);
            }
        }
    }

    @NotNull
    private JsonObject read() {
        if (Files.notExists(this.file)) return new JsonObject();

        try (Reader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root != null && root.isJsonObject()) return root.getAsJsonObject();
            LOGGER.warn("[JUST ZOOM] Ignoring persistence data in {} because its root is not a JSON object.", this.file);
        } catch (IOException | JsonParseException ex) {
            LOGGER.warn("[JUST ZOOM] Failed to read persistence data from {}.", this.file, ex);
        }

        return new JsonObject();
    }

    private boolean write() {
        Path temporaryFile = null;
        try {
            Path parent = this.file.getParent();
            Files.createDirectories(parent);
            temporaryFile = Files.createTempFile(parent, this.file.getFileName().toString(), ".tmp");
            Files.writeString(temporaryFile, GSON.toJson(this.values) + System.lineSeparator(), StandardCharsets.UTF_8);
            this.replaceWith(temporaryFile);
            return true;
        } catch (IOException | RuntimeException ex) {
            LOGGER.error("[JUST ZOOM] Failed to write persistence data to {}.", this.file, ex);
            return false;
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ex) {
                    LOGGER.warn("[JUST ZOOM] Failed to clean up temporary persistence file {}.", temporaryFile, ex);
                }
            }
        }
    }

    private void replaceWith(@NotNull Path temporaryFile) throws IOException {
        // The temporary file lives beside the target so the atomic move cannot cross file-system boundaries.
        try {
            Files.move(temporaryFile, this.file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporaryFile, this.file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static final class DataKey<T> {

        private final String name;
        private final Class<T> type;

        private DataKey(@NotNull String name, @NotNull Class<T> type) {
            this.name = Objects.requireNonNull(name);
            this.type = Objects.requireNonNull(type);
        }

    }

}
