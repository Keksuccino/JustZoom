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
    void defaultsStartAndStopAnimationSpeedsToRequestedValues() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("animation-speed-defaults.json");

        Options options = new Options(configFile.toFile(), null);

        assertEquals(0.45F, options.startZoomingAnimationSpeed.getValue());
        assertEquals(0.2F, options.stopZoomingAnimationSpeed.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals(0.45F, storedZoomOptions.get("start_zooming_animation_speed").getAsFloat());
        assertEquals(0.2F, storedZoomOptions.get("stop_zooming_animation_speed").getAsFloat());
    }

    @Test
    void removesTheDevelopmentTransitionSpeedNames() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("development-transition-speed-names.json");
        Files.writeString(configFile, "{\"zoom\":{\"zoom_in_transition_speed\":1.0,\"zoom_out_transition_speed\":1.0}}", StandardCharsets.UTF_8);

        Options options = new Options(configFile.toFile(), null);

        assertEquals(0.45F, options.startZoomingAnimationSpeed.getValue());
        assertEquals(0.2F, options.stopZoomingAnimationSpeed.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertFalse(storedZoomOptions.has("zoom_in_transition_speed"));
        assertFalse(storedZoomOptions.has("zoom_out_transition_speed"));
    }

    @Test
    void persistsAnimationSpeedsIndependently() {
        Path configFile = this.temporaryDirectory.resolve("animation-speeds.json");
        Options options = new Options(configFile.toFile(), null);

        options.startZoomingAnimationSpeed.setValue(0.35F);
        options.stopZoomingAnimationSpeed.setValue(4.75F);
        Options reloadedOptions = new Options(configFile.toFile(), null);

        assertEquals(0.35F, reloadedOptions.startZoomingAnimationSpeed.getValue());
        assertEquals(4.75F, reloadedOptions.stopZoomingAnimationSpeed.getValue());
    }

    @Test
    void normalizesAnimationSpeedsToTheirSupportedRangeAndStep() {
        assertEquals(0.0F, Options.normalizeAnimationSpeed(-1.0F, 0.45F));
        assertEquals(5.0F, Options.normalizeAnimationSpeed(6.0F, 0.45F));
        assertEquals(0.1F, Options.normalizeAnimationSpeed(0.12F, 0.45F));
        assertEquals(0.15F, Options.normalizeAnimationSpeed(0.13F, 0.45F));
        assertEquals(0.45F, Options.normalizeAnimationSpeed(Float.NaN, 0.45F));
        assertEquals(0.2F, Options.normalizeAnimationSpeed(Float.POSITIVE_INFINITY, 0.2F));
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
