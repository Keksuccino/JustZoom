package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionsScreenTest {

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
        List<?> keyMappings = OptionsScreen.KEYBIND_SETTINGS.stream().map(OptionsScreen.KeybindSetting::keyMapping).toList();

        assertEquals(List.of(KeyMappings.KEY_TOGGLE_ZOOM, KeyMappings.KEY_ZOOM_IN, KeyMappings.KEY_ZOOM_OUT, KeyMappings.KEY_OPEN_OPTIONS), keyMappings);
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
