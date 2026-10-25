package de.keksuccino.justzoom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerZoomRulesTest {

    @TempDir
    Path temporaryDirectory;

    @AfterEach
    void restoreDefaultRules() {
        ServerZoomRules.load(new ServerOptions(this.temporaryDirectory.resolve("default-config.json").toFile()));
    }

    @Test
    void loadsTheStandaloneZoomRuleFromTheConfigFile() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("config.json");
        Files.writeString(configFile, "{\"server\":{\"allow_standalone_zoom\":false}}", StandardCharsets.UTF_8);

        ServerZoomRules.load(new ServerOptions(configFile.toFile()));

        assertFalse(ServerZoomRules.getRules().allowStandaloneZoom());
    }

    @Test
    void appliesFileEditsOnlyAfterTheNextLoad() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("config.json");
        ServerZoomRules.load(new ServerOptions(configFile.toFile()));
        Files.writeString(configFile, "{\"server\":{\"allow_standalone_zoom\":false}}", StandardCharsets.UTF_8);

        assertTrue(ServerZoomRules.getRules().allowStandaloneZoom());

        ServerZoomRules.load(new ServerOptions(configFile.toFile()));

        assertFalse(ServerZoomRules.getRules().allowStandaloneZoom());
    }

}
