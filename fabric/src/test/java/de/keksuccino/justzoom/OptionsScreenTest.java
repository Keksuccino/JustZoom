package de.keksuccino.justzoom;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void generalOptionResetStateTracksWhetherTheValueDiffersFromItsDefault() {
        Options options = new Options(this.temporaryDirectory.resolve("options.json").toFile());

        assertTrue(OptionsScreen.isOptionDefault(options.baseZoomFactor));

        options.baseZoomFactor.setValue(50);
        assertFalse(OptionsScreen.isOptionDefault(options.baseZoomFactor));

        options.baseZoomFactor.resetToDefault();
        assertTrue(OptionsScreen.isOptionDefault(options.baseZoomFactor));
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
    void smoothZoomScrollSpeedSliderUsesTheCompleteRangeInPointZeroOneSteps() {
        double sliderStep = 1.0D / 999.0D;

        assertEquals(0.01F, OptionsScreen.sliderValueToSmoothZoomScrollSpeed(-1.0D));
        assertEquals(0.01F, OptionsScreen.sliderValueToSmoothZoomScrollSpeed(0.0D));
        assertEquals(0.02F, OptionsScreen.sliderValueToSmoothZoomScrollSpeed(sliderStep));
        assertEquals(1.0F, OptionsScreen.sliderValueToSmoothZoomScrollSpeed(99.0D / 999.0D));
        assertEquals(10.0F, OptionsScreen.sliderValueToSmoothZoomScrollSpeed(1.0D));
        assertEquals(10.0F, OptionsScreen.sliderValueToSmoothZoomScrollSpeed(2.0D));
        assertEquals(Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, OptionsScreen.sliderValueToSmoothZoomScrollSpeed(Double.NaN));
    }

    @Test
    void smoothZoomScrollSpeedSliderSnapsContinuousInputAndNormalizesStoredValues() {
        double sliderStep = 1.0D / 999.0D;

        assertEquals(0.0D, OptionsScreen.smoothZoomScrollSpeedToSliderValue(-1.0F, 1.0F));
        assertEquals(99.0D / 999.0D, OptionsScreen.smoothZoomScrollSpeedToSliderValue(1.0F, 1.0F), 0.000000001D);
        assertEquals(1.0D, OptionsScreen.smoothZoomScrollSpeedToSliderValue(11.0F, 1.0F));
        assertEquals(99.0D / 999.0D, OptionsScreen.smoothZoomScrollSpeedToSliderValue(Float.NaN, 1.0F), 0.000000001D);
        assertEquals(sliderStep, OptionsScreen.snapSmoothZoomScrollSpeedSliderValue(sliderStep * 1.4D), 0.000000001D);
        assertEquals(sliderStep * 2.0D, OptionsScreen.snapSmoothZoomScrollSpeedSliderValue(sliderStep * 1.5D), 0.000000001D);
    }

    @Test
    void smoothZoomScrollSpeedUsesTimesNotationWithoutUnnecessaryHundredths() {
        assertEquals("0.01", OptionsScreen.formatSmoothZoomScrollSpeed(0.01F));
        assertEquals("1.0", OptionsScreen.formatSmoothZoomScrollSpeed(1.0F));
        assertEquals("1.5", OptionsScreen.formatSmoothZoomScrollSpeed(1.5F));
        assertEquals("1.23", OptionsScreen.formatSmoothZoomScrollSpeed(1.23F));
        assertEquals("10.0", OptionsScreen.formatSmoothZoomScrollSpeed(10.0F));
    }

    @Test
    void zoomStepSizeSliderUsesWholePercentagesAcrossTheCompleteRange() {
        double sliderStep = 1.0D / 399.0D;

        assertEquals(1, OptionsScreen.sliderValueToZoomStepSizePercentage(-1.0D));
        assertEquals(1, OptionsScreen.sliderValueToZoomStepSizePercentage(0.0D));
        assertEquals(2, OptionsScreen.sliderValueToZoomStepSizePercentage(sliderStep));
        assertEquals(100, OptionsScreen.sliderValueToZoomStepSizePercentage(99.0D / 399.0D));
        assertEquals(400, OptionsScreen.sliderValueToZoomStepSizePercentage(1.0D));
        assertEquals(400, OptionsScreen.sliderValueToZoomStepSizePercentage(2.0D));
        assertEquals(Options.DEFAULT_ZOOM_STEP_SIZE_PERCENTAGE, OptionsScreen.sliderValueToZoomStepSizePercentage(Double.NaN));
    }

    @Test
    void zoomStepSizeSliderSnapsContinuousInputAndNormalizesStoredValues() {
        double sliderStep = 1.0D / 399.0D;

        assertEquals(0.0D, OptionsScreen.zoomStepSizePercentageToSliderValue(0));
        assertEquals(99.0D / 399.0D, OptionsScreen.zoomStepSizePercentageToSliderValue(100), 0.000000001D);
        assertEquals(1.0D, OptionsScreen.zoomStepSizePercentageToSliderValue(401));
        assertEquals(sliderStep, OptionsScreen.snapZoomStepSizeSliderValue(sliderStep * 1.4D), 0.000000001D);
        assertEquals(sliderStep * 2.0D, OptionsScreen.snapZoomStepSizeSliderValue(sliderStep * 1.5D), 0.000000001D);
    }

    @Test
    void zoomFactorSlidersUseWholePercentageStepsAcrossTheCompleteRange() {
        assertEquals(0, OptionsScreen.sliderValueToZoomFactorPercentage(-1.0D));
        assertEquals(0, OptionsScreen.sliderValueToZoomFactorPercentage(0.0D));
        assertEquals(0, OptionsScreen.sliderValueToZoomFactorPercentage(0.0049D));
        assertEquals(1, OptionsScreen.sliderValueToZoomFactorPercentage(0.005D));
        assertEquals(1, OptionsScreen.sliderValueToZoomFactorPercentage(0.01D));
        assertEquals(1, OptionsScreen.sliderValueToZoomFactorPercentage(0.0149D));
        assertEquals(2, OptionsScreen.sliderValueToZoomFactorPercentage(0.015D));
        assertEquals(43, OptionsScreen.sliderValueToZoomFactorPercentage(0.43D));
        assertEquals(100, OptionsScreen.sliderValueToZoomFactorPercentage(1.0D));
        assertEquals(100, OptionsScreen.sliderValueToZoomFactorPercentage(2.0D));
        assertEquals(100, OptionsScreen.sliderValueToZoomFactorPercentage(Double.NaN));
    }

    @Test
    void zoomFactorSlidersSnapContinuousInputToWholePercentagePositions() {
        assertEquals(0.0D, OptionsScreen.snapZoomFactorSliderValue(0.0049D));
        assertEquals(0.01D, OptionsScreen.snapZoomFactorSliderValue(0.005D));
        assertEquals(0.01D, OptionsScreen.snapZoomFactorSliderValue(0.0149D));
        assertEquals(0.02D, OptionsScreen.snapZoomFactorSliderValue(0.015D));
        assertEquals(0.43D, OptionsScreen.snapZoomFactorSliderValue(0.4321D));
        assertEquals(1.0D, OptionsScreen.snapZoomFactorSliderValue(Double.NaN));
    }

    @Test
    void zoomFactorPercentageNormalizesBeforeBecomingASliderValue() {
        assertEquals(0.0D, OptionsScreen.zoomFactorPercentageToSliderValue(-1));
        assertEquals(0.43D, OptionsScreen.zoomFactorPercentageToSliderValue(43), 0.000000001D);
        assertEquals(1.0D, OptionsScreen.zoomFactorPercentageToSliderValue(101));
    }

    @Test
    void zoomPreviewRequiresAnInWorldSelectedAdvancedTabAndRecentSliderMovement() {
        assertTrue(OptionsScreen.shouldActivateZoomPreview(true, true, true));
        assertFalse(OptionsScreen.shouldActivateZoomPreview(false, true, true));
        assertFalse(OptionsScreen.shouldActivateZoomPreview(true, false, true));
        assertFalse(OptionsScreen.shouldActivateZoomPreview(true, true, false));
    }

    @Test
    void zoomPreviewStartsOnMovementAndLingersForOneSecond() {
        TestNanoClock clock = new TestNanoClock();
        OptionsScreen.ZoomPreviewTimer timer = new OptionsScreen.ZoomPreviewTimer(clock);

        assertFalse(timer.isActive());

        timer.recordMovement();
        assertTrue(timer.isActive());

        clock.advanceNanos(OptionsScreen.ZOOM_PREVIEW_LINGER_NANOS);
        assertTrue(timer.isActive());

        clock.advanceNanos(1L);
        assertFalse(timer.isActive());
    }

    @Test
    void zoomPreviewMovementRefreshesTheLingerWindow() {
        TestNanoClock clock = new TestNanoClock();
        OptionsScreen.ZoomPreviewTimer timer = new OptionsScreen.ZoomPreviewTimer(clock);
        timer.recordMovement();
        clock.advanceNanos(OptionsScreen.ZOOM_PREVIEW_LINGER_NANOS);

        timer.recordMovement();
        clock.advanceNanos(OptionsScreen.ZOOM_PREVIEW_LINGER_NANOS);

        assertTrue(timer.isActive());
        clock.advanceNanos(1L);
        assertFalse(timer.isActive());
    }

    @Test
    void zoomPreviewTimerCanBeResetWhenTheScreenLifecycleRestarts() {
        TestNanoClock clock = new TestNanoClock();
        OptionsScreen.ZoomPreviewTimer timer = new OptionsScreen.ZoomPreviewTimer(clock);
        timer.recordMovement();

        timer.reset();

        assertFalse(timer.isActive());
    }

    @Test
    void spyglassOverlayCycleUsesOrangeExceptWhenNeverSelected() {
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.ONLY_SPYGLASS));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.ONLY_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.SPYGLASS_AND_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.NEVER_CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.NEVER));
    }

    @Test
    void showHudCycleUsesOrangeExceptWhenNeverSelected() {
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.showHudValueColor(ShowHudMode.ONLY_SPYGLASS));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.showHudValueColor(ShowHudMode.ONLY_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.showHudValueColor(ShowHudMode.SPYGLASS_AND_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.NEVER_CYCLE_VALUE_COLOR, OptionsScreen.showHudValueColor(ShowHudMode.NEVER));
    }

    @Test
    void spyglassSoundsCycleUsesOrangeExceptWhenNeverSelected() {
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassSoundsValueColor(SpyglassSoundsMode.ONLY_SPYGLASS));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassSoundsValueColor(SpyglassSoundsMode.ONLY_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassSoundsValueColor(SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.NEVER_CYCLE_VALUE_COLOR, OptionsScreen.spyglassSoundsValueColor(SpyglassSoundsMode.NEVER));
    }

    @Test
    void controlsTabContainsEveryJustZoomKeybind() {
        List<KeyMapping> keyMappings = OptionsScreen.KEYBIND_SETTINGS.stream().map(OptionsScreen.KeybindSetting::keyMapping).toList();

        assertEquals(List.of(KeyMappings.KEY_TOGGLE_ZOOM, KeyMappings.KEY_ZOOM_IN, KeyMappings.KEY_ZOOM_OUT), keyMappings);
    }

    private static KeyMapping keyMapping(String suffix, int defaultKey) {
        return new KeyMapping("justzoom.test." + suffix, defaultKey, CATEGORY);
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
