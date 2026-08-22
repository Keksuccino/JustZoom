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

            Options options = new Options(configFile.toFile());

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

        Options options = new Options(configFile.toFile());

        assertEquals(ShowHudMode.NEVER, options.showHud.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals("never", storedZoomOptions.get("show_hud").getAsString());
    }

    @Test
    void preservesTheRetiredMirroredViewOption() throws IOException {
        for (boolean retiredValue : new boolean[]{false, true}) {
            Path configFile = this.temporaryDirectory.resolve("retired-mirrored-view-option-" + retiredValue + ".json");
            Files.writeString(configFile, "{\"zoom\":{\"allow_zoom_in_mirrored_view\":" + retiredValue + "}}", StandardCharsets.UTF_8);

            new Options(configFile.toFile());

            JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
            assertEquals(retiredValue, storedZoomOptions.get("allow_zoom_in_mirrored_view").getAsBoolean());
        }
    }

    @Test
    void defaultsImproveThirdPersonZoomToEnabled() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("third-person-default.json");

        Options options = new Options(configFile.toFile());

        assertTrue(options.improveThirdPersonZoom.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertTrue(storedZoomOptions.get("improve_third_person_zoom").getAsBoolean());
    }

    @Test
    void defaultsStartAndStopAnimationSpeedsToRequestedValues() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("animation-speed-defaults.json");

        Options options = new Options(configFile.toFile());

        assertEquals(Options.DEFAULT_START_ZOOMING_ANIMATION_SPEED, options.startZoomingAnimationSpeed.getValue());
        assertEquals(Options.DEFAULT_STOP_ZOOMING_ANIMATION_SPEED, options.stopZoomingAnimationSpeed.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals(Options.DEFAULT_START_ZOOMING_ANIMATION_SPEED, storedZoomOptions.get("start_zooming_animation_speed").getAsFloat());
        assertEquals(Options.DEFAULT_STOP_ZOOMING_ANIMATION_SPEED, storedZoomOptions.get("stop_zooming_animation_speed").getAsFloat());
    }

    @Test
    void defaultsSmoothZoomScrollSpeedToOneTimes() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("smooth-zoom-scroll-speed-default.json");

        Options options = new Options(configFile.toFile());

        assertEquals(Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, options.smoothZoomScrollSpeed.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals(Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, storedZoomOptions.get("smooth_zoom_scroll_speed").getAsFloat());
    }

    @Test
    void defaultsBaseZoomFactorToSeventyFivePercent() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("base-zoom-factor-default.json");

        Options options = new Options(configFile.toFile());

        assertEquals(Options.DEFAULT_BASE_ZOOM_FACTOR_PERCENTAGE, options.baseZoomFactor.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals(Options.DEFAULT_BASE_ZOOM_FACTOR_PERCENTAGE, storedZoomOptions.get("base_zoom_factor").getAsInt());
    }

    @Test
    void persistsBaseZoomFactorPercentage() {
        Path configFile = this.temporaryDirectory.resolve("base-zoom-factor.json");
        Options options = new Options(configFile.toFile());

        options.baseZoomFactor.setValue(37);
        Options reloadedOptions = new Options(configFile.toFile());

        assertEquals(37, reloadedOptions.baseZoomFactor.getValue());
    }

    @Test
    void doesNotCarryOverRetiredBaseMagnification() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("retired-base-magnification.json");
        Files.writeString(configFile, "{\"zoom\":{\"base_magnification\":4.0}}", StandardCharsets.UTF_8);

        Options options = new Options(configFile.toFile());

        assertEquals(Options.DEFAULT_BASE_ZOOM_FACTOR_PERCENTAGE, options.baseZoomFactor.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals(4.0F, storedZoomOptions.get("base_magnification").getAsFloat());
        assertEquals(Options.DEFAULT_BASE_ZOOM_FACTOR_PERCENTAGE, storedZoomOptions.get("base_zoom_factor").getAsInt());
    }

    @Test
    void defaultsMaximumZoomFactorToOneHundredPercent() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("maximum-zoom-factor-default.json");

        Options options = new Options(configFile.toFile());

        assertEquals(Options.DEFAULT_MAXIMUM_ZOOM_FACTOR_PERCENTAGE, options.maximumZoomFactor.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals(Options.DEFAULT_MAXIMUM_ZOOM_FACTOR_PERCENTAGE, storedZoomOptions.get("maximum_zoom_factor").getAsInt());
    }

    @Test
    void persistsMaximumZoomFactorPercentage() {
        Path configFile = this.temporaryDirectory.resolve("maximum-zoom-factor.json");
        Options options = new Options(configFile.toFile());

        options.maximumZoomFactor.setValue(37);
        Options reloadedOptions = new Options(configFile.toFile());

        assertEquals(37, reloadedOptions.maximumZoomFactor.getValue());
    }

    @Test
    void preservesTheDevelopmentTransitionSpeedNames() throws IOException {
        Path configFile = this.temporaryDirectory.resolve("development-transition-speed-names.json");
        Files.writeString(configFile, "{\"zoom\":{\"zoom_in_transition_speed\":1.0,\"zoom_out_transition_speed\":1.0}}", StandardCharsets.UTF_8);

        Options options = new Options(configFile.toFile());

        assertEquals(Options.DEFAULT_START_ZOOMING_ANIMATION_SPEED, options.startZoomingAnimationSpeed.getValue());
        assertEquals(Options.DEFAULT_STOP_ZOOMING_ANIMATION_SPEED, options.stopZoomingAnimationSpeed.getValue());
        JsonObject storedZoomOptions = JsonParser.parseString(Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("zoom");
        assertEquals(1.0F, storedZoomOptions.get("zoom_in_transition_speed").getAsFloat());
        assertEquals(1.0F, storedZoomOptions.get("zoom_out_transition_speed").getAsFloat());
    }

    @Test
    void persistsAnimationSpeedsIndependently() {
        Path configFile = this.temporaryDirectory.resolve("animation-speeds.json");
        Options options = new Options(configFile.toFile());

        options.startZoomingAnimationSpeed.setValue(0.35F);
        options.stopZoomingAnimationSpeed.setValue(4.75F);
        Options reloadedOptions = new Options(configFile.toFile());

        assertEquals(0.35F, reloadedOptions.startZoomingAnimationSpeed.getValue());
        assertEquals(4.75F, reloadedOptions.stopZoomingAnimationSpeed.getValue());
    }

    @Test
    void persistsSmoothZoomScrollSpeedIndependentlyFromAnimationDurations() {
        Path configFile = this.temporaryDirectory.resolve("smooth-zoom-scroll-speed.json");
        Options options = new Options(configFile.toFile());

        options.smoothZoomScrollSpeed.setValue(2.75F);
        options.startZoomingAnimationSpeed.setValue(0.35F);
        options.stopZoomingAnimationSpeed.setValue(4.75F);
        Options reloadedOptions = new Options(configFile.toFile());

        assertEquals(2.75F, reloadedOptions.smoothZoomScrollSpeed.getValue());
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
    void normalizesSmoothZoomScrollSpeedToItsSupportedRangeAndStep() {
        assertEquals(0.01F, Options.normalizeSmoothZoomScrollSpeed(-1.0F, 1.0F));
        assertEquals(0.01F, Options.normalizeSmoothZoomScrollSpeed(0.01F, 1.0F));
        assertEquals(1.23F, Options.normalizeSmoothZoomScrollSpeed(1.234F, 1.0F));
        assertEquals(1.24F, Options.normalizeSmoothZoomScrollSpeed(1.235F, 1.0F));
        assertEquals(10.0F, Options.normalizeSmoothZoomScrollSpeed(11.0F, 1.0F));
        assertEquals(2.75F, Options.normalizeSmoothZoomScrollSpeed(Float.NaN, 2.75F));
        assertEquals(1.0F, Options.normalizeSmoothZoomScrollSpeed(Float.POSITIVE_INFINITY, 1.0F));
    }

    @Test
    void normalizesZoomFactorsToPercentageRange() {
        assertEquals(0, Options.normalizeZoomFactorPercentage(-1));
        assertEquals(0, Options.normalizeZoomFactorPercentage(0));
        assertEquals(43, Options.normalizeZoomFactorPercentage(43));
        assertEquals(100, Options.normalizeZoomFactorPercentage(100));
        assertEquals(100, Options.normalizeZoomFactorPercentage(101));
    }

    @Test
    void persistsDisabledImproveThirdPersonZoom() {
        Path configFile = this.temporaryDirectory.resolve("third-person-disabled.json");
        Options options = new Options(configFile.toFile());

        options.improveThirdPersonZoom.setValue(false);
        Options reloadedOptions = new Options(configFile.toFile());

        assertFalse(reloadedOptions.improveThirdPersonZoom.getValue());
    }

}
