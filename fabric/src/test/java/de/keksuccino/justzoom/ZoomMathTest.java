package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZoomMathTest {

    private static final double DOUBLE_TOLERANCE = 0.000000001D;

    @Test
    void clampsFinalFovToSupportedRange() {
        assertEquals(ZoomMath.MIN_FOV, ZoomMath.clampFov(0.01F));
        assertEquals(70.0F, ZoomMath.clampFov(70.0F));
        assertEquals(ZoomMath.MAX_FOV, ZoomMath.clampFov(200.0F));
    }

    @Test
    void leavesFovUnchangedAtOneTimesMagnification() {
        assertEquals(70.0F, ZoomMath.calculateZoomedFov(70.0F, ZoomMath.MIN_MAGNIFICATION));
    }

    @Test
    void calculatesOpticalMagnificationFromHalfAngles() {
        float zoomedFov = ZoomMath.calculateZoomedFov(70.0F, 4.0D);

        assertEquals(19.85826F, zoomedFov, 0.00001F);
        assertEquals(4.0D, ZoomMath.calculateEffectiveMagnification(70.0F, zoomedFov), 0.000001D);
    }

    @Test
    void reachesOneTenthDegreeMaximumZoom() {
        double maximumMagnification = ZoomMath.calculateMaximumMagnification(70.0F);

        assertEquals(ZoomMath.MIN_FOV, ZoomMath.calculateZoomedFov(70.0F, maximumMagnification));
        assertEquals(802.37853D, maximumMagnification, 0.001D);
    }

    @Test
    void scalesMaximumMagnificationAcrossTheConfiguredPercentageRange() {
        double fullMaximum = ZoomMath.calculateMaximumMagnification(70.0F);

        assertEquals(ZoomMath.MIN_MAGNIFICATION, ZoomMath.calculateMaximumMagnification(70.0F, 0), DOUBLE_TOLERANCE);
        assertEquals(ZoomMath.MIN_MAGNIFICATION + (fullMaximum - ZoomMath.MIN_MAGNIFICATION) * 0.5D, ZoomMath.calculateMaximumMagnification(70.0F, 50), DOUBLE_TOLERANCE);
        assertEquals(fullMaximum, ZoomMath.calculateMaximumMagnification(70.0F, 100), DOUBLE_TOLERANCE);
    }

    @Test
    void clampsMaximumMagnificationPercentageBeforeScaling() {
        assertEquals(ZoomMath.MIN_MAGNIFICATION, ZoomMath.calculateMaximumMagnification(70.0F, -1), DOUBLE_TOLERANCE);
        assertEquals(ZoomMath.calculateMaximumMagnification(70.0F), ZoomMath.calculateMaximumMagnification(70.0F, 101), DOUBLE_TOLERANCE);
    }

    @Test
    void appliesReciprocalWheelSteps() {
        double zoomedIn = ZoomMath.applyScroll(4.0D, 1.0D, 1.5D, 1000.0D);
        double zoomedBackOut = ZoomMath.applyScroll(zoomedIn, -1.0D, 1.5D, 1000.0D);

        assertEquals(6.0D, zoomedIn, DOUBLE_TOLERANCE);
        assertEquals(4.0D, zoomedBackOut, DOUBLE_TOLERANCE);
    }

    @Test
    void appliesFractionalWheelMovementProportionally() {
        assertEquals(4.0D * Math.sqrt(1.5D), ZoomMath.applyScroll(4.0D, 0.5D, 1.5D, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void clampsWheelMovementAtBothLimits() {
        assertEquals(100.0D, ZoomMath.applyScroll(80.0D, 10.0D, 1.5D, 100.0D), DOUBLE_TOLERANCE);
        assertEquals(ZoomMath.MIN_MAGNIFICATION, ZoomMath.applyScroll(2.0D, -10.0D, 1.5D, 100.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void movesAtAConstantLogarithmicRateInEitherDirection() {
        assertEquals(1.2D, ZoomMath.moveMagnificationTowards(1.0D, 10.0D, 1.2D), DOUBLE_TOLERANCE);
        assertEquals(10.0D / 1.2D, ZoomMath.moveMagnificationTowards(10.0D, 1.0D, 1.2D), DOUBLE_TOLERANCE);
        assertEquals(1.1D, ZoomMath.moveMagnificationTowards(1.0D, 1.1D, 1.2D), DOUBLE_TOLERANCE);
    }

    @Test
    void interpolatesMagnificationGeometrically() {
        assertEquals(2.0D, ZoomMath.interpolateMagnification(1.0D, 4.0D, 0.5F), DOUBLE_TOLERANCE);
    }

    @Test
    void scalesMouseMovementByActualMagnification() {
        assertEquals(0.25D, ZoomMath.calculateMouseSensitivityScale(4.0D), DOUBLE_TOLERANCE);
        assertEquals(1.0D, ZoomMath.calculateMouseSensitivityScale(1.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void normalizesInvalidConfigurationValues() {
        assertEquals(4.0D, ZoomMath.normalizeMagnification(Double.NaN, 4.0D, 100.0D), DOUBLE_TOLERANCE);
        assertEquals(100.0D, ZoomMath.normalizeMagnification(Double.POSITIVE_INFINITY, 4.0D, 100.0D), DOUBLE_TOLERANCE);
        assertEquals(1.5D, ZoomMath.normalizeScrollMagnificationMultiplier(0.5D, 1.5D), DOUBLE_TOLERANCE);
        assertEquals(ZoomMath.MAX_SCROLL_MAGNIFICATION_MULTIPLIER, ZoomMath.normalizeScrollMagnificationMultiplier(100.0D, 1.5D), DOUBLE_TOLERANCE);
    }

}
