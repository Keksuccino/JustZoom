package de.keksuccino.justzoom;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerOptionsTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void allowsStandaloneZoomByDefaultAndWritesTheServerSection() throws IOException {
        Path configFile = this.getConfigFile();

        ServerOptions options = new ServerOptions(configFile.toFile());

        assertTrue(options.allowStandaloneZoom.getValue());
        assertTrue(this.readServerSection(configFile).get("allow_standalone_zoom").getAsBoolean());
    }

    @Test
    void readsAStandaloneZoomRuleEditedInTheFile() throws IOException {
        Path configFile = this.getConfigFile();
        Files.writeString(configFile, "{\"server\":{\"allow_standalone_zoom\":false}}", StandardCharsets.UTF_8);

        assertFalse(new ServerOptions(configFile.toFile()).allowStandaloneZoom.getValue());
    }

    @Test
    void repairsAnInvalidStandaloneZoomRuleWithTheDefault() throws IOException {
        Path configFile = this.getConfigFile();
        Files.writeString(configFile, "{\"server\":{\"allow_standalone_zoom\":\"no\"}}", StandardCharsets.UTF_8);

        assertTrue(new ServerOptions(configFile.toFile()).allowStandaloneZoom.getValue());
        assertTrue(this.readServerSection(configFile).get("allow_standalone_zoom").getAsBoolean());
    }

    @Test
    void sharesTheConfigFileWithTheClientOptions() throws IOException {
        Path configFile = this.getConfigFile();
        Options clientOptions = new Options(configFile.toFile());
        clientOptions.baseZoomFactor.setValue(40);

        new ServerOptions(configFile.toFile());

        JsonObject storedData = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals(40, storedData.getAsJsonObject("zoom").get("base_zoom_factor").getAsInt());
        assertTrue(storedData.getAsJsonObject("server").get("allow_standalone_zoom").getAsBoolean());
        assertEquals(40, new Options(configFile.toFile()).baseZoomFactor.getValue());
    }

    @Test
    void savingClientOptionsKeepsTheServerRuleEditedInTheFile() throws IOException {
        Path configFile = this.getConfigFile();
        Options clientOptions = new Options(configFile.toFile());
        new ServerOptions(configFile.toFile());
        String editedFile = Files.readString(configFile, StandardCharsets.UTF_8).replace("\"allow_standalone_zoom\": true", "\"allow_standalone_zoom\": false");
        Files.writeString(configFile, editedFile, StandardCharsets.UTF_8);

        clientOptions.baseZoomFactor.setValue(60);
        clientOptions.smoothZoomInOut.setValue(false);

        assertFalse(new ServerOptions(configFile.toFile()).allowStandaloneZoom.getValue());
        assertFalse(this.readServerSection(configFile).get("allow_standalone_zoom").getAsBoolean());
        assertEquals(60, new Options(configFile.toFile()).baseZoomFactor.getValue());
    }

    private Path getConfigFile() {
        return this.temporaryDirectory.resolve("config.json");
    }

    private JsonObject readServerSection(Path configFile) throws IOException {
        return JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("server");
    }

}
