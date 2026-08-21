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

class OptionsTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void discardsLegacySpyglassOverlayStateRegardlessOfItsValue() throws IOException {
        for (boolean legacyValue : new boolean[]{false, true}) {
            Path configFile = this.temporaryDirectory.resolve("config-" + legacyValue + ".json");
            Files.writeString(configFile, "{\"spyglass\":{\"show_spyglass_overlay\":" + legacyValue + "}}", StandardCharsets.UTF_8);

            Options options = new Options(configFile.toFile(), null);

            assertEquals(SpyglassOverlayMode.ONLY_SPYGLASS, options.spyglassOverlay.getValue());
            assertEquals(SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM, options.spyglassSounds.getValue());
            JsonObject storedSpyglassOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("spyglass");
            assertFalse(storedSpyglassOptions.has("show_spyglass_overlay"));
            assertEquals("only_spyglass", storedSpyglassOptions.get("spyglass_overlay").getAsString());
            assertEquals("spyglass_and_keybind_zoom", storedSpyglassOptions.get("spyglass_sounds").getAsString());
        }
    }

}
