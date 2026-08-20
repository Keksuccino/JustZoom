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

        assertEquals(4.0F, persistenceData.lastMagnification.getValueOrDefault(4.0F));
        assertFalse(Files.exists(this.getPersistenceFile()));
    }

    @Test
    void savesAndReloadsTypedValues() {
        PersistenceData persistenceData = this.createPersistenceData();

        persistenceData.lastMagnification.setValue(6.0F);

        PersistenceData reloadedData = this.createPersistenceData();
        assertEquals(6.0F, reloadedData.lastMagnification.getValueOrDefault(4.0F));
    }

    @Test
    void preservesUnknownValuesWhenSavingKnownValues() throws IOException {
        Files.writeString(this.getPersistenceFile(), "{\n  \"future_value\": true\n}\n", StandardCharsets.UTF_8);
        PersistenceData persistenceData = this.createPersistenceData();

        persistenceData.lastMagnification.setValue(5.0F);

        JsonObject savedData = JsonParser.parseString(Files.readString(this.getPersistenceFile(), StandardCharsets.UTF_8)).getAsJsonObject();
        assertTrue(savedData.get("future_value").getAsBoolean());
        assertEquals(5.0F, savedData.get("last_magnification").getAsFloat());
    }

    @Test
    void recoversFromMalformedDataOnTheNextWrite() throws IOException {
        Files.writeString(this.getPersistenceFile(), "not valid json", StandardCharsets.UTF_8);
        PersistenceData persistenceData = this.createPersistenceData();

        assertEquals(4.0F, persistenceData.lastMagnification.getValueOrDefault(4.0F));
        persistenceData.lastMagnification.setValue(5.0F);

        PersistenceData reloadedData = this.createPersistenceData();
        assertEquals(5.0F, reloadedData.lastMagnification.getValueOrDefault(4.0F));
    }

    @Test
    void returnsFallbackForAValueWithTheWrongType() throws IOException {
        Files.writeString(this.getPersistenceFile(), "{\n  \"last_magnification\": \"invalid\"\n}\n", StandardCharsets.UTF_8);
        PersistenceData persistenceData = this.createPersistenceData();

        assertEquals(4.0F, persistenceData.lastMagnification.getValueOrDefault(4.0F));
    }

    @Test
    void retriesAnUnsuccessfulWriteOnTheNextSetterCall() throws IOException {
        Path blockedParent = this.temporaryDirectory.resolve("blocked_parent");
        Files.writeString(blockedParent, "not a directory", StandardCharsets.UTF_8);
        PersistenceData persistenceData = new PersistenceData(blockedParent.resolve("persistence_data.json").toFile());

        persistenceData.lastMagnification.setValue(6.0F);
        assertEquals(4.0F, persistenceData.lastMagnification.getValueOrDefault(4.0F));
        Files.delete(blockedParent);
        Files.createDirectory(blockedParent);
        persistenceData.lastMagnification.setValue(6.0F);

        PersistenceData reloadedData = new PersistenceData(blockedParent.resolve("persistence_data.json").toFile());
        assertEquals(6.0F, reloadedData.lastMagnification.getValueOrDefault(4.0F));
    }

    private PersistenceData createPersistenceData() {
        return new PersistenceData(this.getPersistenceFile().toFile());
    }

    private Path getPersistenceFile() {
        return this.temporaryDirectory.resolve("persistence_data.json");
    }

}
