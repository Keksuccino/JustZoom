package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoomMathTest {

    @Test
    void preservesVanillaMinimumOutsideExpandedZoomRange() {
        assertEquals(ZoomMath.VANILLA_MIN_FOV_MODIFIER, ZoomMath.selectFovModifierMinimum(false, ZoomMath.VANILLA_MIN_FOV_MODIFIER));
    }

    @Test
    void usesJustZoomMinimumInsideExpandedZoomRange() {
        assertEquals(ZoomMath.MIN_FOV_MODIFIER, ZoomMath.selectFovModifierMinimum(true, ZoomMath.VANILLA_MIN_FOV_MODIFIER));
    }

    @Test
    void clampsModifierToSupportedRange() {
        assertEquals(ZoomMath.MIN_FOV_MODIFIER, ZoomMath.clampFovModifier(0.0F));
        assertEquals(0.25F, ZoomMath.clampFovModifier(0.25F));
        assertEquals(ZoomMath.MAX_FOV_MODIFIER, ZoomMath.clampFovModifier(2.0F));
    }

    @Test
    void clampsFinalFovToSafeRange() {
        assertEquals(ZoomMath.MIN_FOV, ZoomMath.clampFov(0.5F));
        assertEquals(70.0F, ZoomMath.clampFov(70.0F));
        assertEquals(ZoomMath.MAX_FOV, ZoomMath.clampFov(200.0F));
    }

    @Test
    void reachesOneDegreeMaximumZoom() {
        assertEquals(ZoomMath.MIN_FOV, ZoomMath.calculateZoomedFov(70.0F, ZoomMath.MIN_FOV_MODIFIER));
    }

    @Test
    void preservesIntermediateZoomLevels() {
        assertEquals(7.0F, ZoomMath.calculateZoomedFov(70.0F, 0.1F));
    }

    @Test
    void clampsFinalFovThroughoutExpandedZoomLifecycle() {
        assertTrue(ZoomMath.shouldClampFinalFov(true, 1.0F, 0.5F));
        assertTrue(ZoomMath.shouldClampFinalFov(false, 0.05F, 0.5F));
        assertTrue(ZoomMath.shouldClampFinalFov(false, 0.5F, 0.05F));
        assertFalse(ZoomMath.shouldClampFinalFov(false, 0.5F, 0.75F));
    }

}
