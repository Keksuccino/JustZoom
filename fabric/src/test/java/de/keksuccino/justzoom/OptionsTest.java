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

class OptionsTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void ignoresLegacySpyglassOverlayStateRegardlessOfItsValue() throws IOException {
        for (boolean legacyValue : new boolean[]{false, true}) {
            Path configFile = this.temporaryDirectory.resolve("config-" + legacyValue + ".json");
            Files.writeString(configFile, "{\"spyglass\":{\"show_spyglass_overlay\":" + legacyValue + "}}", StandardCharsets.UTF_8);

            Options options = new Options(configFile.toFile(), null);

            assertEquals(SpyglassOverlayMode.ONLY_SPYGLASS, options.spyglassOverlay.getValue());
            assertEquals(SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM, options.spyglassSounds.getValue());
            JsonObject storedSpyglassOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("spyglass");
            assertEquals(legacyValue, storedSpyglassOptions.get("show_spyglass_overlay").getAsBoolean());
            assertEquals("only_spyglass", storedSpyglassOptions.get("spyglass_overlay").getAsString());
            assertEquals("spyglass_and_keybind_zoom", storedSpyglassOptions.get("spyglass_sounds").getAsString());
        }
    }

    @Test
    void defaultsShowHudToNever() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("new-config.json");

        Options options = new Options(configFile.toFile(), null);

        assertEquals(ShowHudMode.NEVER, options.showHud.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals("never", storedZoomOptions.get("show_hud").getAsString());
    }

    @Test
    void removesTheRetiredMirroredViewOption() throws IOException {
        for (boolean retiredValue : new boolean[]{false, true}) {
            Path configFile = this.temporaryDirectory.resolve("retired-mirrored-view-option-" + retiredValue + ".json");
            Files.writeString(configFile, "{\"zoom\":{\"allow_zoom_in_mirrored_view\":" + retiredValue + "}}", StandardCharsets.UTF_8);

            new Options(configFile.toFile(), null);

            JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
            assertFalse(storedZoomOptions.has("allow_zoom_in_mirrored_view"));
        }
    }

    @Test
    void defaultsImproveThirdPersonZoomToEnabled() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("third-person-default.json");

        Options options = new Options(configFile.toFile(), null);

        assertTrue(options.improveThirdPersonZoom.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertTrue(storedZoomOptions.get("improve_third_person_zoom").getAsBoolean());
    }

    @Test
    void persistsDisabledImproveThirdPersonZoom() {
        Path configFile = this.temporaryDirectory.resolve("third-person-disabled.json");
        Options options = new Options(configFile.toFile(), null);

        options.improveThirdPersonZoom.setValue(false);
        Options reloadedOptions = new Options(configFile.toFile(), null);

        assertFalse(reloadedOptions.improveThirdPersonZoom.getValue());
    }

}
