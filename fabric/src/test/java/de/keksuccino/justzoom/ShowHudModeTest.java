package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShowHudModeTest {

    @Test
    void cyclesThroughEveryModeInDisplayOrder() {
        assertSame(ShowHudMode.ONLY_KEYBIND_ZOOM, ShowHudMode.ONLY_SPYGLASS.next());
        assertSame(ShowHudMode.SPYGLASS_AND_KEYBIND_ZOOM, ShowHudMode.ONLY_KEYBIND_ZOOM.next());
        assertSame(ShowHudMode.NEVER, ShowHudMode.SPYGLASS_AND_KEYBIND_ZOOM.next());
        assertSame(ShowHudMode.ONLY_SPYGLASS, ShowHudMode.NEVER.next());
    }

    @Test
    void onlySpyglassShowsTheHudForSpyglassZoom() {
        assertFalse(ShowHudMode.ONLY_SPYGLASS.shouldHide(true, false));
        assertTrue(ShowHudMode.ONLY_SPYGLASS.shouldHide(false, true));
        assertFalse(ShowHudMode.ONLY_SPYGLASS.shouldHide(true, true));
    }

    @Test
    void onlyKeybindZoomShowsTheHudForKeybindZoom() {
        assertTrue(ShowHudMode.ONLY_KEYBIND_ZOOM.shouldHide(true, false));
        assertFalse(ShowHudMode.ONLY_KEYBIND_ZOOM.shouldHide(false, true));
        assertFalse(ShowHudMode.ONLY_KEYBIND_ZOOM.shouldHide(true, true));
    }

    @Test
    void combinedModeShowsTheHudForBothZoomInputs() {
        assertFalse(ShowHudMode.SPYGLASS_AND_KEYBIND_ZOOM.shouldHide(true, false));
        assertFalse(ShowHudMode.SPYGLASS_AND_KEYBIND_ZOOM.shouldHide(false, true));
        assertFalse(ShowHudMode.SPYGLASS_AND_KEYBIND_ZOOM.shouldHide(true, true));
    }

    @Test
    void neverModeHidesTheHudForBothZoomInputs() {
        assertTrue(ShowHudMode.NEVER.shouldHide(true, false));
        assertTrue(ShowHudMode.NEVER.shouldHide(false, true));
        assertTrue(ShowHudMode.NEVER.shouldHide(true, true));
    }

    @Test
    void everyModeLeavesTheHudAloneOutsideZooming() {
        for (ShowHudMode mode : ShowHudMode.values()) {
            assertFalse(mode.shouldHide(false, false));
        }
    }

}
