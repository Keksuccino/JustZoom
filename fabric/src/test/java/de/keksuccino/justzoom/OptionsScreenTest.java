package de.keksuccino.justzoom;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionsScreenTest {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("justzoom", "test.options_screen"));

    @TempDir
    Path temporaryDirectory;

    @Test
    void detectsChangedKeyThatMatchesAnotherBinding() {
        KeyMapping zoom = keyMapping("changed_zoom", InputConstants.KEY_X);
        KeyMapping other = keyMapping("other_zoom", InputConstants.KEY_Z);
        zoom.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_Z));

        assertTrue(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom, other}));
    }

    @Test
    void detectsDefaultKeyThatMatchesAnotherChangedBinding() {
        KeyMapping zoom = keyMapping("default_zoom", InputConstants.KEY_Z);
        KeyMapping other = keyMapping("changed_other", InputConstants.KEY_X);
        other.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_Z));

        assertTrue(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom, other}));
    }

    @Test
    void ignoresMatchingDefaultBindingsUntilOneIsChanged() {
        KeyMapping first = keyMapping("first_default", InputConstants.KEY_Z);
        KeyMapping second = keyMapping("second_default", InputConstants.KEY_Z);

        assertFalse(OptionsScreen.hasKeybindCollision(first, new KeyMapping[]{first, second}));
    }

    @Test
    void ignoresDifferentBindings() {
        KeyMapping zoom = keyMapping("different_zoom", InputConstants.KEY_Z);
        KeyMapping other = keyMapping("different_other", InputConstants.KEY_X);

        assertFalse(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom, other}));
    }

    @Test
    void ignoresUnboundBindings() {
        KeyMapping zoom = keyMapping("unbound_zoom", InputConstants.KEY_Z);
        KeyMapping other = keyMapping("unbound_other", InputConstants.KEY_X);
        zoom.setKey(InputConstants.UNKNOWN);

        assertFalse(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom, other}));
    }

    @Test
    void ignoresTheBindingItself() {
        KeyMapping zoom = keyMapping("self_zoom", InputConstants.KEY_Z);
        zoom.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_X));

        assertFalse(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom}));
    }

    @Test
    void generalFloatInputAndResetButtonFillTheOptionRowWidth() {
        int controlWidth = OptionsScreen.calculatePrimaryControlWidth(360);
        OptionsScreen.FloatInputWidths widths = OptionsScreen.calculateFloatInputWidths(controlWidth, 100);

        assertEquals(100, widths.labelWidth());
        assertEquals(200, widths.inputWidth());
        assertEquals(360, widths.labelWidth() + OptionsScreen.FLOAT_INPUT_GAP + widths.inputWidth() + OptionsScreen.CONTROL_GAP + OptionsScreen.RESET_BUTTON_WIDTH);
    }

    @Test
    void longFloatInputLabelLeavesTheInputUsable() {
        OptionsScreen.FloatInputWidths widths = OptionsScreen.calculateFloatInputWidths(100, 100);

        assertEquals(55, widths.labelWidth());
        assertEquals(OptionsScreen.FLOAT_INPUT_MIN_WIDTH, widths.inputWidth());
        assertEquals(100, widths.labelWidth() + OptionsScreen.FLOAT_INPUT_GAP + widths.inputWidth());
    }

    @Test
    void generalOptionResetStateTracksWhetherTheValueDiffersFromItsDefault() {
        Options options = new Options(this.temporaryDirectory.resolve("options.json").toFile());

        assertTrue(OptionsScreen.isOptionDefault(options.baseMagnification));

        options.baseMagnification.setValue(8.0F);
        assertFalse(OptionsScreen.isOptionDefault(options.baseMagnification));

        options.baseMagnification.resetToDefault();
        assertTrue(OptionsScreen.isOptionDefault(options.baseMagnification));
    }

    @Test
    void floatInputResetStateTreatsEquivalentTextAsDefaultAndInvalidTextAsChanged() {
        Options options = new Options(this.temporaryDirectory.resolve("float-input-options.json").toFile());

        assertTrue(OptionsScreen.isFloatInputDefault(options.baseMagnification, "4.0"));
        assertTrue(OptionsScreen.isFloatInputDefault(options.baseMagnification, "4"));
        assertFalse(OptionsScreen.isFloatInputDefault(options.baseMagnification, "invalid"));

        options.baseMagnification.setValue(8.0F);
        assertFalse(OptionsScreen.isFloatInputDefault(options.baseMagnification, "4.0"));
    }

    @Test
    void animationSpeedSliderUsesTheCompleteRangeInPointZeroFiveSecondSteps() {
        assertEquals(0.0F, OptionsScreen.sliderValueToAnimationSpeed(-1.0D));
        assertEquals(0.0F, OptionsScreen.sliderValueToAnimationSpeed(0.0D));
        assertEquals(0.05F, OptionsScreen.sliderValueToAnimationSpeed(0.01D));
        assertEquals(0.2F, OptionsScreen.sliderValueToAnimationSpeed(0.04D));
        assertEquals(0.45F, OptionsScreen.sliderValueToAnimationSpeed(0.09D));
        assertEquals(1.0F, OptionsScreen.sliderValueToAnimationSpeed(0.2D));
        assertEquals(5.0F, OptionsScreen.sliderValueToAnimationSpeed(1.0D));
        assertEquals(5.0F, OptionsScreen.sliderValueToAnimationSpeed(2.0D));
    }

    @Test
    void animationSpeedSliderNormalizesStoredValuesWithTheSettingSpecificFallback() {
        assertEquals(0.0D, OptionsScreen.animationSpeedToSliderValue(-1.0F, 0.45F));
        assertEquals(0.09D, OptionsScreen.animationSpeedToSliderValue(0.45F, 0.45F), 0.000000001D);
        assertEquals(0.04D, OptionsScreen.animationSpeedToSliderValue(0.2F, 0.2F), 0.000000001D);
        assertEquals(1.0D, OptionsScreen.animationSpeedToSliderValue(6.0F, 0.45F));
        assertEquals(0.09D, OptionsScreen.animationSpeedToSliderValue(Float.NaN, 0.45F), 0.000000001D);
        assertEquals(0.04D, OptionsScreen.animationSpeedToSliderValue(Float.NaN, 0.2F), 0.000000001D);
        assertEquals(Options.DEFAULT_START_ZOOMING_ANIMATION_SPEED, OptionsScreen.sliderValueToAnimationSpeed(Double.NaN));
    }

    @Test
    void maximumZoomFactorSliderUsesWholePercentageStepsAcrossTheCompleteRange() {
        assertEquals(0, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(-1.0D));
        assertEquals(0, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(0.0D));
        assertEquals(0, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(0.0049D));
        assertEquals(1, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(0.005D));
        assertEquals(1, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(0.01D));
        assertEquals(1, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(0.0149D));
        assertEquals(2, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(0.015D));
        assertEquals(43, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(0.43D));
        assertEquals(100, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(1.0D));
        assertEquals(100, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(2.0D));
        assertEquals(100, OptionsScreen.sliderValueToMaximumZoomFactorPercentage(Double.NaN));
    }

    @Test
    void maximumZoomFactorSliderSnapsContinuousInputToWholePercentagePositions() {
        assertEquals(0.0D, OptionsScreen.snapMaximumZoomFactorSliderValue(0.0049D));
        assertEquals(0.01D, OptionsScreen.snapMaximumZoomFactorSliderValue(0.005D));
        assertEquals(0.01D, OptionsScreen.snapMaximumZoomFactorSliderValue(0.0149D));
        assertEquals(0.02D, OptionsScreen.snapMaximumZoomFactorSliderValue(0.015D));
        assertEquals(0.43D, OptionsScreen.snapMaximumZoomFactorSliderValue(0.4321D));
        assertEquals(1.0D, OptionsScreen.snapMaximumZoomFactorSliderValue(Double.NaN));
    }

    @Test
    void maximumZoomFactorPercentageNormalizesBeforeBecomingASliderValue() {
        assertEquals(0.0D, OptionsScreen.maximumZoomFactorPercentageToSliderValue(-1));
        assertEquals(0.43D, OptionsScreen.maximumZoomFactorPercentageToSliderValue(43), 0.000000001D);
        assertEquals(1.0D, OptionsScreen.maximumZoomFactorPercentageToSliderValue(101));
    }

    @Test
    void maximumZoomPreviewRequiresAnInWorldSelectedAdvancedTabAndRecentSliderMovement() {
        assertTrue(OptionsScreen.shouldActivateMaximumZoomPreview(true, true, true));
        assertFalse(OptionsScreen.shouldActivateMaximumZoomPreview(false, true, true));
        assertFalse(OptionsScreen.shouldActivateMaximumZoomPreview(true, false, true));
        assertFalse(OptionsScreen.shouldActivateMaximumZoomPreview(true, true, false));
    }

    @Test
    void maximumZoomPreviewStartsOnMovementAndLingersForOneSecond() {
        TestNanoClock clock = new TestNanoClock();
        OptionsScreen.MaximumZoomPreviewTimer timer = new OptionsScreen.MaximumZoomPreviewTimer(clock);

        assertFalse(timer.isActive());

        timer.recordMovement();
        assertTrue(timer.isActive());

        clock.advanceNanos(OptionsScreen.MAXIMUM_ZOOM_PREVIEW_LINGER_NANOS);
        assertTrue(timer.isActive());

        clock.advanceNanos(1L);
        assertFalse(timer.isActive());
    }

    @Test
    void maximumZoomPreviewMovementRefreshesTheLingerWindow() {
        TestNanoClock clock = new TestNanoClock();
        OptionsScreen.MaximumZoomPreviewTimer timer = new OptionsScreen.MaximumZoomPreviewTimer(clock);
        timer.recordMovement();
        clock.advanceNanos(OptionsScreen.MAXIMUM_ZOOM_PREVIEW_LINGER_NANOS);

        timer.recordMovement();
        clock.advanceNanos(OptionsScreen.MAXIMUM_ZOOM_PREVIEW_LINGER_NANOS);

        assertTrue(timer.isActive());
        clock.advanceNanos(1L);
        assertFalse(timer.isActive());
    }

    @Test
    void maximumZoomPreviewTimerCanBeResetWhenTheScreenLifecycleRestarts() {
        TestNanoClock clock = new TestNanoClock();
        OptionsScreen.MaximumZoomPreviewTimer timer = new OptionsScreen.MaximumZoomPreviewTimer(clock);
        timer.recordMovement();

        timer.reset();

        assertFalse(timer.isActive());
    }

    @Test
    void previewOpacityIncludesInputSettingLabels() {
        StringWidget inputSettingLabel = new StringWidget(100, 9, Component.literal("Input label"), nonRenderingFont());

        OptionsScreen.updatePreviewControlOpacity(inputSettingLabel, null, OptionsScreen.PREVIEW_CONTROL_OPACITY);

        assertEquals(OptionsScreen.PREVIEW_CONTROL_OPACITY, inputSettingLabel.getAlpha());
    }

    @Test
    void spyglassOverlayCycleUsesOrangeExceptWhenDisabled() {
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.ONLY_SPYGLASS));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.ONLY_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.SPYGLASS_AND_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.DISABLED_CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.DISABLED));
    }

    @Test
    void showHudCycleUsesOrangeExceptWhenNeverSelected() {
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.showHudValueColor(ShowHudMode.ONLY_SPYGLASS));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.showHudValueColor(ShowHudMode.ONLY_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.showHudValueColor(ShowHudMode.SPYGLASS_AND_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.DISABLED_CYCLE_VALUE_COLOR, OptionsScreen.showHudValueColor(ShowHudMode.NEVER));
    }

    @Test
    void spyglassSoundsCycleUsesOrangeExceptWhenDisabled() {
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassSoundsValueColor(SpyglassSoundsMode.ONLY_SPYGLASS));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassSoundsValueColor(SpyglassSoundsMode.ONLY_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassSoundsValueColor(SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.DISABLED_CYCLE_VALUE_COLOR, OptionsScreen.spyglassSoundsValueColor(SpyglassSoundsMode.DISABLED));
    }

    @Test
    void controlsTabContainsEveryJustZoomKeybind() {
        List<KeyMapping> keyMappings = OptionsScreen.KEYBIND_SETTINGS.stream().map(OptionsScreen.KeybindSetting::keyMapping).toList();

        assertEquals(List.of(KeyMappings.KEY_TOGGLE_ZOOM, KeyMappings.KEY_ZOOM_IN, KeyMappings.KEY_ZOOM_OUT), keyMappings);
    }

    @Test
    void everyBundledLanguageNamesTheAdvancedTabAndMaximumZoomFactor() throws IOException {
        for (String language : List.of("de_de", "en_us", "es_mx", "fr_fr", "ja_jp", "ko_kr", "ru_ru", "tr_tr", "uk_ua", "zh_cn")) {
            String resourcePath = "assets/justzoom/lang/" + language + ".json";
            try (InputStream stream = OptionsScreenTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
                assertNotNull(stream, resourcePath);
                JsonObject translations = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                assertTrue(translations.has("justzoom.options.tab.advanced"), language);
                assertFalse(translations.get("justzoom.options.tab.advanced").getAsString().isBlank(), language);
                assertTrue(translations.has("justzoom.options.maximum_zoom_factor"), language);
                assertFalse(translations.get("justzoom.options.maximum_zoom_factor").getAsString().isBlank(), language);
                assertTrue(translations.has("justzoom.options.maximum_zoom_factor.desc"), language);
                assertFalse(translations.get("justzoom.options.maximum_zoom_factor.desc").getAsString().isBlank(), language);
            }
        }
    }

    private static KeyMapping keyMapping(String suffix, int defaultKey) {
        return new KeyMapping("justzoom.test." + suffix, defaultKey, CATEGORY);
    }

    private static Font nonRenderingFont() {
        return new Font(new Font.Provider() {

            @Override
            public GlyphSource glyphs(FontDescription font) {
                throw new AssertionError("This opacity-only test must not render text");
            }

            @Override
            public EffectGlyph effect() {
                throw new AssertionError("This opacity-only test must not render text");
            }

        });
    }

    private static final class TestNanoClock implements LongSupplier {

        private long nowNanos;

        @Override
        public long getAsLong() {
            return this.nowNanos;
        }

        void advanceNanos(long nanos) {
            this.nowNanos += nanos;
        }

    }

}
