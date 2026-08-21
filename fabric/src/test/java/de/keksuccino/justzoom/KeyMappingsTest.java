package de.keksuccino.justzoom;

import com.mojang.blaze3d.platform.InputConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyMappingsTest {

    @Test
    void usesSerializableWheelDirectionsAsDefaults() {
        assertEquals("key.mouse.9", KeyMappings.KEY_ZOOM_IN.getDefaultKey().getName());
        assertEquals("key.mouse.10", KeyMappings.KEY_ZOOM_OUT.getDefaultKey().getName());
        assertEquals(InputConstants.Type.MOUSE, KeyMappings.KEY_ZOOM_IN.getDefaultKey().getType());
        assertEquals(InputConstants.Type.MOUSE, KeyMappings.KEY_ZOOM_OUT.getDefaultKey().getType());
    }

    @Test
    void resolvesPhysicalWheelDirections() {
        assertEquals(KeyMappings.KEY_ZOOM_IN.getDefaultKey(), KeyMappings.getMouseWheelKey(1.0D));
        assertEquals(KeyMappings.KEY_ZOOM_OUT.getDefaultKey(), KeyMappings.getMouseWheelKey(-1.0D));
        assertEquals(InputConstants.UNKNOWN, KeyMappings.getMouseWheelKey(0.0D));
        assertEquals(InputConstants.UNKNOWN, KeyMappings.getMouseWheelKey(Double.NaN));
        assertEquals(InputConstants.UNKNOWN, KeyMappings.getMouseWheelKey(Double.POSITIVE_INFINITY));
    }

    @Test
    void matchesOnlyTheConfiguredWheelDirection() {
        assertTrue(KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_IN, 1.0D));
        assertFalse(KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_IN, -1.0D));
        assertTrue(KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_OUT, -1.0D));
        assertFalse(KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_OUT, 1.0D));
        assertFalse(KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_OUT, 0.0D));
        assertFalse(KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_OUT, Double.NaN));
        assertFalse(KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_IN, Double.POSITIVE_INFINITY));
    }

    @Test
    void acceptsOnlyFiniteNonZeroWheelDirections() {
        assertTrue(KeyMappings.hasMouseWheelDirection(1.0D));
        assertTrue(KeyMappings.hasMouseWheelDirection(-1.0D));
        assertFalse(KeyMappings.hasMouseWheelDirection(0.0D));
        assertFalse(KeyMappings.hasMouseWheelDirection(Double.NaN));
        assertFalse(KeyMappings.hasMouseWheelDirection(Double.NEGATIVE_INFINITY));
    }

    @Test
    void identifiesOnlyTheTwoAdjustmentBindingsAsWheelCapable() {
        assertTrue(KeyMappings.isZoomAdjustment(KeyMappings.KEY_ZOOM_IN));
        assertTrue(KeyMappings.isZoomAdjustment(KeyMappings.KEY_ZOOM_OUT));
        assertFalse(KeyMappings.isZoomAdjustment(KeyMappings.KEY_TOGGLE_ZOOM));
    }

}
