package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoomHandlerTest {

    @Test
    void activatesZoomForTheNormalKeybind() {
        assertTrue(ZoomHandler.ZoomInput.isActive(true, false, false));
    }

    @Test
    void activatesZoomForSpyglassWhenReplacementIsEnabled() {
        assertTrue(ZoomHandler.ZoomInput.isActive(false, true, true));
    }

    @Test
    void leavesSpyglassOutOfZoomWhenReplacementIsDisabled() {
        assertFalse(ZoomHandler.ZoomInput.isActive(false, true, false));
    }

    @Test
    void staysInactiveWithoutEitherZoomInput() {
        assertFalse(ZoomHandler.ZoomInput.isActive(false, false, true));
    }

    @Test
    void keepsTheNormalKeybindActiveWhileUsingTheSpyglassSettingIsDisabled() {
        assertTrue(ZoomHandler.ZoomInput.isActive(true, true, false));
    }

}
