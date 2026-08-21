package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void combinesQueuedZoomAdjustmentClicks() {
        assertEquals(2, ZoomHandler.ZoomInput.calculateKeyAdjustment(3, 1));
        assertEquals(-2, ZoomHandler.ZoomInput.calculateKeyAdjustment(1, 3));
        assertEquals(0, ZoomHandler.ZoomInput.calculateKeyAdjustment(2, 2));
    }

    @Test
    void ignoresInvalidNegativeClickCounts() {
        assertEquals(2, ZoomHandler.ZoomInput.calculateKeyAdjustment(2, -1));
        assertEquals(-2, ZoomHandler.ZoomInput.calculateKeyAdjustment(-1, 2));
    }

    @Test
    void scrollTriggersFollowTheirBoundActionInsteadOfThePhysicalDirection() {
        assertEquals(2.5D, ZoomHandler.ZoomInput.calculateScrollAdjustment(true, false, -2.5D));
        assertEquals(-2.5D, ZoomHandler.ZoomInput.calculateScrollAdjustment(false, true, 2.5D));
    }

    @Test
    void ignoresAmbiguousAndInvalidScrollInput() {
        assertEquals(0.0D, ZoomHandler.ZoomInput.calculateScrollAdjustment(false, false, 1.0D));
        assertEquals(0.0D, ZoomHandler.ZoomInput.calculateScrollAdjustment(true, true, 1.0D));
        assertEquals(0.0D, ZoomHandler.ZoomInput.calculateScrollAdjustment(true, false, 0.0D));
        assertEquals(0.0D, ZoomHandler.ZoomInput.calculateScrollAdjustment(true, false, Double.NaN));
        assertEquals(0.0D, ZoomHandler.ZoomInput.calculateScrollAdjustment(true, false, Double.POSITIVE_INFINITY));
    }

}
