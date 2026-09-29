package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpyglassOverlayModeTest {

    @Test
    void cyclesThroughEveryModeInDisplayOrder() {
        assertSame(SpyglassOverlayMode.ONLY_KEYBIND_ZOOM, SpyglassOverlayMode.ONLY_SPYGLASS.next());
        assertSame(SpyglassOverlayMode.SPYGLASS_AND_KEYBIND_ZOOM, SpyglassOverlayMode.ONLY_KEYBIND_ZOOM.next());
        assertSame(SpyglassOverlayMode.NEVER, SpyglassOverlayMode.SPYGLASS_AND_KEYBIND_ZOOM.next());
        assertSame(SpyglassOverlayMode.ONLY_SPYGLASS, SpyglassOverlayMode.NEVER.next());
    }

    @Test
    void onlySpyglassIgnoresKeybindZoom() {
        assertTrue(SpyglassOverlayMode.ONLY_SPYGLASS.shouldShow(true, false));
        assertFalse(SpyglassOverlayMode.ONLY_SPYGLASS.shouldShow(false, true));
    }

    @Test
    void onlyKeybindZoomIgnoresSpyglass() {
        assertFalse(SpyglassOverlayMode.ONLY_KEYBIND_ZOOM.shouldShow(true, false));
        assertTrue(SpyglassOverlayMode.ONLY_KEYBIND_ZOOM.shouldShow(false, true));
    }

    @Test
    void combinedModeSupportsBothInputs() {
        assertTrue(SpyglassOverlayMode.SPYGLASS_AND_KEYBIND_ZOOM.shouldShow(true, false));
        assertTrue(SpyglassOverlayMode.SPYGLASS_AND_KEYBIND_ZOOM.shouldShow(false, true));
        assertTrue(SpyglassOverlayMode.SPYGLASS_AND_KEYBIND_ZOOM.shouldShow(true, true));
    }

    @Test
    void neverModeIgnoresBothInputs() {
        assertFalse(SpyglassOverlayMode.NEVER.shouldShow(true, false));
        assertFalse(SpyglassOverlayMode.NEVER.shouldShow(false, true));
        assertFalse(SpyglassOverlayMode.NEVER.shouldShow(true, true));
    }

}
