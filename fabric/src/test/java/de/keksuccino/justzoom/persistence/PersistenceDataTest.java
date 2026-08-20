package de.keksuccino.justzoom.persistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.keksuccino.justzoom.PersistenceData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceDataTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void returnsFallbackWhenFileDoesNotExist() {
        PersistenceData persistenceData = this.createPersistenceData();

        assertEquals(0.25F, persistenceData.lastZoomLevel.getValueOrDefault(0.25F));
        assertFalse(Files.exists(this.getPersistenceFile()));
    }

    @Test
    void savesAndReloadsTypedValues() {
        PersistenceData persistenceData = this.createPersistenceData();

        persistenceData.lastZoomLevel.setValue(0.4F);

        PersistenceData reloadedData = this.createPersistenceData();
        assertEquals(0.4F, reloadedData.lastZoomLevel.getValueOrDefault(0.25F));
    }

    @Test
    void preservesUnknownValuesWhenSavingKnownValues() throws IOException {
        Files.writeString(this.getPersistenceFile(), "{\n  \"future_value\": true\n}\n", StandardCharsets.UTF_8);
        PersistenceData persistenceData = this.createPersistenceData();

        persistenceData.lastZoomLevel.setValue(0.3F);

        JsonObject savedData = JsonParser.parseString(Files.readString(this.getPersistenceFile(), StandardCharsets.UTF_8)).getAsJsonObject();
        assertTrue(savedData.get("future_value").getAsBoolean());
        assertEquals(0.3F, savedData.get("last_zoom_level").getAsFloat());
    }

    @Test
    void recoversFromMalformedDataOnTheNextWrite() throws IOException {
        Files.writeString(this.getPersistenceFile(), "not valid json", StandardCharsets.UTF_8);
        PersistenceData persistenceData = this.createPersistenceData();

        assertEquals(0.25F, persistenceData.lastZoomLevel.getValueOrDefault(0.25F));
        persistenceData.lastZoomLevel.setValue(0.2F);

        PersistenceData reloadedData = this.createPersistenceData();
        assertEquals(0.2F, reloadedData.lastZoomLevel.getValueOrDefault(0.25F));
    }

    @Test
    void returnsFallbackForAValueWithTheWrongType() throws IOException {
        Files.writeString(this.getPersistenceFile(), "{\n  \"last_zoom_level\": \"invalid\"\n}\n", StandardCharsets.UTF_8);
        PersistenceData persistenceData = this.createPersistenceData();

        assertEquals(0.25F, persistenceData.lastZoomLevel.getValueOrDefault(0.25F));
    }

    @Test
    void retriesAnUnsuccessfulWriteOnTheNextSetterCall() throws IOException {
        Path blockedParent = this.temporaryDirectory.resolve("blocked_parent");
        Files.writeString(blockedParent, "not a directory", StandardCharsets.UTF_8);
        PersistenceData persistenceData = new PersistenceData(blockedParent.resolve("persistence_data.json").toFile());

        persistenceData.lastZoomLevel.setValue(0.4F);
        assertEquals(0.25F, persistenceData.lastZoomLevel.getValueOrDefault(0.25F));
        Files.delete(blockedParent);
        Files.createDirectory(blockedParent);
        persistenceData.lastZoomLevel.setValue(0.4F);

        PersistenceData reloadedData = new PersistenceData(blockedParent.resolve("persistence_data.json").toFile());
        assertEquals(0.4F, reloadedData.lastZoomLevel.getValueOrDefault(0.25F));
    }

    private PersistenceData createPersistenceData() {
        return new PersistenceData(this.getPersistenceFile().toFile());
    }

    private Path getPersistenceFile() {
        return this.temporaryDirectory.resolve("persistence_data.json");
    }

}
