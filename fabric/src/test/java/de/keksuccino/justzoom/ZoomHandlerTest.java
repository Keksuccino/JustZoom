package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ZoomHandlerTest {

    private static final double DOUBLE_TOLERANCE = 0.000000001D;

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
    void keepsZoomAvailableForEveryCameraView() {
        assertTrue(ZoomHandler.isZoomAvailable(false));
    }

    @Test
    void blocksZoomWhileAScreenIsOpen() {
        assertFalse(ZoomHandler.isZoomAvailable(true));
    }

    @Test
    void baseZoomPreviewUsesTheConfiguredBasePercentage() {
        double previewMagnification = ZoomHandler.calculateZoomPreviewMagnification(70.0F, 75, 100, OptionsScreen.ZoomPreviewTarget.BASE_ZOOM);

        assertEquals(ZoomMath.calculateMagnification(70.0F, 75), previewMagnification, DOUBLE_TOLERANCE);
    }

    @Test
    void baseZoomPreviewRespectsTheMaximumZoomLimit() {
        double previewMagnification = ZoomHandler.calculateZoomPreviewMagnification(70.0F, 75, 50, OptionsScreen.ZoomPreviewTarget.BASE_ZOOM);

        assertEquals(ZoomMath.calculateMagnification(70.0F, 50), previewMagnification, DOUBLE_TOLERANCE);
    }

    @Test
    void maximumZoomPreviewUsesTheMaximumPercentageIndependentlyOfBaseZoom() {
        double previewMagnification = ZoomHandler.calculateZoomPreviewMagnification(70.0F, 25, 80, OptionsScreen.ZoomPreviewTarget.MAXIMUM_ZOOM);

        assertEquals(ZoomMath.calculateMagnification(70.0F, 80), previewMagnification, DOUBLE_TOLERANCE);
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

    @Test
    void extractsEnabledSpyglassOverlayWhenZoomHidesTheHud() {
        assertTrue(ZoomHandler.shouldExtractSpyglassOverlaySeparately(false, true, true));
    }

    @Test
    void leavesSpyglassOverlayControlledByItsOwnSetting() {
        assertFalse(ZoomHandler.shouldExtractSpyglassOverlaySeparately(false, true, false));
    }

    @Test
    void respectsVanillaHudHiding() {
        assertFalse(ZoomHandler.shouldExtractSpyglassOverlaySeparately(true, true, true));
    }

    @Test
    void usesTheNormalExtractionPositionWhileTheHudIsVisible() {
        assertFalse(ZoomHandler.shouldExtractSpyglassOverlaySeparately(false, false, true));
    }

    @Test
    void preservesVanillaSpyglassAnimationWhenJustZoomIsInactive() {
        boolean justZoomZooming = ZoomHandler.ZoomInput.isActive(false, true, false);

        assertEquals(0.73F, ZoomHandler.calculateSpyglassOverlayScale(justZoomZooming, 0.9D, 0.73F));
    }

    @Test
    void usesCustomSpyglassAnimationForBothJustZoomInputs() {
        boolean keybindZooming = ZoomHandler.ZoomInput.isActive(true, false, false);
        boolean replacedSpyglassZooming = ZoomHandler.ZoomInput.isActive(false, true, true);

        assertEquals(0.8125F, ZoomHandler.calculateSpyglassOverlayScale(keybindZooming, 0.5D, 0.73F));
        assertEquals(0.8125F, ZoomHandler.calculateSpyglassOverlayScale(replacedSpyglassZooming, 0.5D, 0.73F));
    }

    @Test
    void synchronizesJustZoomSpyglassAnimationWithZoomProgress() {
        assertEquals(0.5F, ZoomHandler.calculateSpyglassOverlayScale(true, 0.0D, 0.73F));
        assertEquals(0.8125F, ZoomHandler.calculateSpyglassOverlayScale(true, 0.5D, 0.73F));
        assertEquals(1.125F, ZoomHandler.calculateSpyglassOverlayScale(true, 1.0D, 0.73F));
    }

    @Test
    void clampsUnexpectedZoomProgressForSpyglassAnimation() {
        assertEquals(0.5F, ZoomHandler.calculateSpyglassOverlayScale(true, -1.0D, 0.73F));
        assertEquals(0.5F, ZoomHandler.calculateSpyglassOverlayScale(true, Double.NaN, 0.73F));
        assertEquals(1.125F, ZoomHandler.calculateSpyglassOverlayScale(true, 2.0D, 0.73F));
    }

    @Test
    void usesFirstPersonCameraForImprovedRearThirdPersonZoom() {
        assertTrue(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> true, true, false));
    }

    @Test
    void keepsRearThirdPersonCameraWhenImprovementIsDisabled() {
        assertFalse(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> true, false, false));
    }

    @Test
    void doesNotOverrideTheCameraOutsideZoom() {
        assertFalse(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> false, true, false));
    }

    @Test
    void doesNotOverrideTheMirroredCamera() {
        assertFalse(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> true, true, true));
    }

    @Test
    void avoidsScopeQueriesWhenTheCameraImprovementIsDisabled() {
        assertFalse(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> fail(), false, false));
    }

    @Test
    void avoidsScopeQueriesForTheMirroredCamera() {
        assertFalse(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> fail(), true, true));
    }

    @ParameterizedTest
    @CsvSource({"true, false, true", "false, false, false", "true, true, true", "false, true, true"})
    void recursiveScopeChecksUseTheUnderlyingCamera(boolean firstPerson, boolean keybindDown, boolean expectedOverride) {
        ScopedZoomInput zooming = new ScopedZoomInput(firstPerson, keybindDown);

        assertEquals(expectedOverride, ZoomHandler.shouldUseFirstPersonCameraWhileZooming(zooming, true, false));
        assertEquals(1, zooming.scopeChecks);
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false"})
    void recursionAlsoTerminatesWhenScopeDetectionStartsTheQuery(boolean firstPerson, boolean expectedZoom) {
        ScopedZoomInput zooming = new ScopedZoomInput(firstPerson, false);

        assertEquals(expectedZoom, zooming.getAsBoolean());
        assertEquals(2, zooming.scopeChecks);
    }

    @Test
    void nestedQueriesKeepTheGuardActiveUntilTheOuterQueryCompletes() {
        assertTrue(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> {
            assertFalse(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> fail(), true, false));
            assertFalse(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> fail(), true, false));
            return true;
        }, true, false));
        assertTrue(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> true, true, false));
    }

    @Test
    void completedInactiveQueryDoesNotBlockLaterZoom() {
        assertFalse(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> false, true, false));
        assertTrue(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> true, true, false));
    }

    @Test
    void failingScopeQueryDoesNotBlockLaterZoom() {
        IllegalStateException failure = new IllegalStateException();
        assertSame(failure, assertThrows(IllegalStateException.class, () -> ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> {
            throw failure;
        }, true, false)));
        assertTrue(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> true, true, false));
    }

    @Test
    void cameraQueriesOnDifferentThreadsDoNotSuppressEachOther() {
        var executor = Executors.newSingleThreadExecutor();
        try {
            assertTrue(ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> {
                try {
                    return executor.submit(() -> ZoomHandler.shouldUseFirstPersonCameraWhileZooming(() -> true, true, false)).get(10, TimeUnit.SECONDS);
                } catch (Exception failure) {
                    throw new AssertionError(failure);
                }
            }, true, false));
        } finally {
            executor.shutdownNow();
        }
    }

    private static final class ScopedZoomInput implements BooleanSupplier {

        private final boolean firstPerson;
        private final boolean keybindDown;
        private int scopeChecks;

        private ScopedZoomInput(boolean firstPerson, boolean keybindDown) {
            this.firstPerson = firstPerson;
            this.keybindDown = keybindDown;
        }

        @Override
        public boolean getAsBoolean() {
            // Model Codex's scope -> camera -> Just Zoom -> scope cycle, bounding a broken
            // implementation so this regression fails before exhausting the Java stack.
            assertTrue(++this.scopeChecks <= 2);
            boolean scoped = ZoomHandler.shouldUseFirstPersonCameraWhileZooming(this, true, false) || this.firstPerson;
            return ZoomHandler.ZoomInput.isActive(this.keybindDown, scoped, true);
        }

    }

}
