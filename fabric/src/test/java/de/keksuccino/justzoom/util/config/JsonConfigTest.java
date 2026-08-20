package de.keksuccino.justzoom.util.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonConfigTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void writesCategorizedDefaultsAndReloadsTypedValues() throws IOException {
        Path file = this.getConfigFile();
        TestConfig config = new TestConfig(file);

        assertEquals(1, config.count.getValue());
        assertTrue(config.enabled.getValue());
        assertEquals(List.of("alpha", "beta"), config.names.getValue());

        JsonObject savedGeneral = this.readJson(file).getAsJsonObject("general");
        assertEquals(1, savedGeneral.get("count").getAsInt());
        assertTrue(savedGeneral.get("enabled").getAsBoolean());
        assertEquals(2, savedGeneral.getAsJsonArray("names").size());

        TestConfig reloadedConfig = new TestConfig(file);
        assertEquals(List.of("alpha", "beta"), reloadedConfig.names.getValue());
    }

    @Test
    void persistsSetUpdateResetAndRemoveOperations() throws IOException {
        Path file = this.getConfigFile();
        TestConfig config = new TestConfig(file);

        config.count.setValue(4).update(value -> value + 3);
        assertEquals(7, new TestConfig(file).count.getValue());

        config.count.resetToDefault();
        assertEquals(1, new TestConfig(file).count.getValue());

        config.count.remove();
        assertFalse(this.readJson(file).getAsJsonObject("general").has("count"));
        assertEquals(1, config.count.getValue());
    }

    @Test
    void supportsOptionalValuesWithoutWritingTheirFallback() throws IOException {
        Path file = this.getConfigFile();
        JsonConfig config = new JsonConfig(file);
        ConfigValue<Float> lastZoomLevel = config.optional("last_zoom_level", Float.class);

        assertEquals(0.25F, lastZoomLevel.getValueOrDefault(0.25F));
        assertNull(lastZoomLevel.getValueOrNull());
        assertFalse(Files.exists(file));

        lastZoomLevel.setValue(0.4F);
        JsonConfig reloadedConfig = new JsonConfig(file);
        assertEquals(0.4F, reloadedConfig.optional("last_zoom_level", Float.class).getValueOrDefault(0.25F));

        lastZoomLevel.remove();
        assertFalse(this.readJson(file).has("last_zoom_level"));
    }

    @Test
    void preservesUnknownValuesWhenSavingKnownValues() throws IOException {
        Path file = this.getConfigFile();
        Files.writeString(file, "{\n  \"future_section\": {\"future_value\": true},\n  \"general\": {\"count\": 5, \"unknown\": \"keep\"}\n}\n", StandardCharsets.UTF_8);

        TestConfig config = new TestConfig(file);
        config.count.setValue(6);

        JsonObject savedData = this.readJson(file);
        assertTrue(savedData.getAsJsonObject("future_section").get("future_value").getAsBoolean());
        assertEquals("keep", savedData.getAsJsonObject("general").get("unknown").getAsString());
    }

    @Test
    void reloadRefreshesCachedValuesAfterAnExternalChange() throws IOException {
        Path file = this.getConfigFile();
        TestConfig config = new TestConfig(file);
        Files.writeString(file, "{\n  \"general\": {\"count\": 12, \"enabled\": false, \"names\": [\"changed\"]}\n}\n", StandardCharsets.UTF_8);

        assertTrue(config.reload());

        assertEquals(12, config.count.getValue());
        assertFalse(config.enabled.getValue());
        assertEquals(List.of("changed"), config.names.getValue());
    }

    @Test
    void repairsMissingAndWrongTypedValuesWithDefaults() throws IOException {
        Path file = this.getConfigFile();
        Files.writeString(file, "{\n  \"general\": {\"count\": \"wrong\", \"enabled\": false}\n}\n", StandardCharsets.UTF_8);

        TestConfig config = new TestConfig(file);

        assertEquals(1, config.count.getValue());
        assertFalse(config.enabled.getValue());
        assertEquals(List.of("alpha", "beta"), config.names.getValue());
        JsonObject savedGeneral = this.readJson(file).getAsJsonObject("general");
        assertTrue(savedGeneral.get("count").getAsJsonPrimitive().isNumber());
        assertEquals(1, savedGeneral.get("count").getAsInt());
    }

    @Test
    void convertsEveryLegacyTypeAndDeletesTheLegacyFile() throws IOException {
        Path file = this.getConfigFile();
        Path legacyFile = this.getLegacyFile();
        Files.writeString(legacyFile, "//Legacy config\n##[numbers]\nI:integer = '7';\nL:long = '9000000000';\nD:double = '1.5';\nF:float = '0.4';\n\n##[other]\nB:enabled = 'false';\nS:message = 'hello\nworld';\n", StandardCharsets.UTF_8);

        JsonConfig config = new JsonConfig(file, legacyFile);
        ConfigSection numbers = config.section("numbers");
        ConfigSection other = config.section("other");

        assertEquals(7, numbers.optional("integer", Integer.class).getValue());
        assertEquals(9000000000L, numbers.optional("long", Long.class).getValue());
        assertEquals(1.5D, numbers.optional("double", Double.class).getValue());
        assertEquals(0.4F, numbers.optional("float", Float.class).getValue());
        assertFalse(other.optional("enabled", Boolean.class).getValue());
        assertEquals("hello\nworld", other.optional("message", String.class).getValue());
        assertTrue(Files.exists(file));
        assertFalse(Files.exists(legacyFile));
    }

    @Test
    void fillsDefaultsForVariablesMissingFromTheLegacyFile() throws IOException {
        Path file = this.getConfigFile();
        Path legacyFile = this.getLegacyFile();
        Files.writeString(legacyFile, "##[general]\nI:count = '8';\n", StandardCharsets.UTF_8);

        TestConfig config = new TestConfig(file, legacyFile);

        assertEquals(8, config.count.getValue());
        assertTrue(config.enabled.getValue());
        assertEquals(List.of("alpha", "beta"), config.names.getValue());
        assertFalse(Files.exists(legacyFile));
    }

    @Test
    void existingJsonTakesPrecedenceOverAStaleLegacyFile() throws IOException {
        Path file = this.getConfigFile();
        Path legacyFile = this.getLegacyFile();
        Files.writeString(file, "{\n  \"general\": {\"count\": 9}\n}\n", StandardCharsets.UTF_8);
        Files.writeString(legacyFile, "##[general]\nI:count = '3';\n", StandardCharsets.UTF_8);

        TestConfig config = new TestConfig(file, legacyFile);

        assertEquals(9, config.count.getValue());
        assertTrue(Files.exists(legacyFile));
    }

    @Test
    void refusesToOverwriteAnUnreadableLegacySource() throws IOException {
        Path file = this.getConfigFile();
        Path legacyFile = this.getLegacyFile();
        Files.createDirectory(legacyFile);

        TestConfig config = new TestConfig(file, legacyFile);

        assertEquals(1, config.count.getValue());
        assertFalse(Files.exists(file));
        assertTrue(Files.isDirectory(legacyFile));
        assertFalse(config.save());
    }

    @Test
    void preservesLegacyFileWhenAnyEntryCannotBeConverted() throws IOException {
        Path file = this.getConfigFile();
        Path legacyFile = this.getLegacyFile();
        Files.writeString(legacyFile, "##[general]\nI:count = '8';\nnot a legacy entry\n", StandardCharsets.UTF_8);

        TestConfig config = new TestConfig(file, legacyFile);

        assertEquals(8, config.count.getValue());
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(legacyFile));
    }

    @Test
    void retriesAnUnsuccessfulWriteWhenTheSameValueIsSetAgain() throws IOException {
        Path blockedParent = this.temporaryDirectory.resolve("blocked_parent");
        Files.writeString(blockedParent, "not a directory", StandardCharsets.UTF_8);
        Path file = blockedParent.resolve("config.json");
        TestConfig config = new TestConfig(file);

        assertFalse(Files.exists(file));
        Files.delete(blockedParent);
        Files.createDirectory(blockedParent);
        config.count.setValue(1);

        assertEquals(1, new TestConfig(file).count.getValue());
    }

    @Test
    void rejectsDuplicateDefinitions() {
        JsonConfig config = new JsonConfig(this.getConfigFile());
        config.option("value", 1);

        assertThrows(IllegalArgumentException.class, () -> config.option("value", 2));
    }

    private Path getConfigFile() {
        return this.temporaryDirectory.resolve("config.json");
    }

    private Path getLegacyFile() {
        return this.temporaryDirectory.resolve("config.txt");
    }

    private JsonObject readJson(Path file) throws IOException {
        return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static final class TestConfig extends JsonConfig {

        private final ConfigSection general = this.section("general");

        private final ConfigValue<Integer> count = this.general.option("count", 1);
        private final ConfigValue<Boolean> enabled = this.general.option("enabled", true);
        private final ConfigValue<List<String>> names = this.general.option("names", new TypeToken<List<String>>() { }.getType(), List.of("alpha", "beta"));

        private TestConfig(Path file) {
            this(file, null);
        }

        private TestConfig(Path file, Path legacyFile) {
            super(file, legacyFile);
            this.save();
        }

    }

}
