package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpyglassSoundsModeTest {

    @Test
    void cyclesThroughEveryModeInDisplayOrder() {
        assertSame(SpyglassSoundsMode.ONLY_KEYBIND_ZOOM, SpyglassSoundsMode.ONLY_SPYGLASS.next());
        assertSame(SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM, SpyglassSoundsMode.ONLY_KEYBIND_ZOOM.next());
        assertSame(SpyglassSoundsMode.DISABLED, SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM.next());
        assertSame(SpyglassSoundsMode.ONLY_SPYGLASS, SpyglassSoundsMode.DISABLED.next());
    }

    @Test
    void onlySpyglassIgnoresKeybindZoom() {
        assertTrue(SpyglassSoundsMode.ONLY_SPYGLASS.shouldPlayForSpyglass());
        assertFalse(SpyglassSoundsMode.ONLY_SPYGLASS.shouldPlayForKeybindZoom());
    }

    @Test
    void onlyKeybindZoomIgnoresSpyglass() {
        assertFalse(SpyglassSoundsMode.ONLY_KEYBIND_ZOOM.shouldPlayForSpyglass());
        assertTrue(SpyglassSoundsMode.ONLY_KEYBIND_ZOOM.shouldPlayForKeybindZoom());
    }

    @Test
    void combinedModeSupportsBothInputs() {
        assertTrue(SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM.shouldPlayForSpyglass());
        assertTrue(SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM.shouldPlayForKeybindZoom());
    }

    @Test
    void disabledModeIgnoresBothInputs() {
        assertFalse(SpyglassSoundsMode.DISABLED.shouldPlayForSpyglass());
        assertFalse(SpyglassSoundsMode.DISABLED.shouldPlayForKeybindZoom());
    }

}
