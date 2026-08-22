package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZoomLevelStateTest {

    private static final double DOUBLE_TOLERANCE = 0.000000001D;

    @TempDir
    Path temporaryDirectory;

    @Test
    void appliesPersistedMagnificationWhenEnabled() {
        PersistenceData persistenceData = this.createPersistenceData();
        persistenceData.lastMagnification.setValue(6.0F);

        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 4.0F, true);

        assertEquals(6.0D, zoomLevelState.getTargetMagnification(1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void usesBaseMagnificationWhenPersistedLevelIsDisabled() {
        PersistenceData persistenceData = this.createPersistenceData();
        persistenceData.lastMagnification.setValue(6.0F);

        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 4.0F, false);

        assertEquals(4.0D, zoomLevelState.getTargetMagnification(1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void followsDynamicBaseMagnificationUntilManuallyAdjusted() {
        double[] baseMagnification = {4.0D};
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), () -> baseMagnification[0], false);

        baseMagnification[0] = 6.0D;
        assertEquals(6.0D, zoomLevelState.getTargetMagnification(1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.adjustMagnification(1.0D, 1.5D, 1000.0D);
        baseMagnification[0] = 10.0D;
        assertEquals(9.0D, zoomLevelState.getTargetMagnification(1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void savesAdjustedMagnification() {
        PersistenceData persistenceData = this.createPersistenceData();
        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 4.0F, false);

        zoomLevelState.adjustMagnification(1.0D, 1.5D, 1000.0D);

        PersistenceData reloadedData = this.createPersistenceData();
        ZoomLevelState restartedState = new ZoomLevelState(reloadedData, 4.0F, true);
        assertEquals(6.0D, restartedState.getTargetMagnification(1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void reciprocalAdjustmentsReturnToTheSameMagnification() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);

        zoomLevelState.adjustMagnification(1.0D, 1.5D, 1000.0D);
        zoomLevelState.adjustMagnification(-1.0D, 1.5D, 1000.0D);

        assertEquals(4.0D, zoomLevelState.getTargetMagnification(1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void zoomingOutLeavesADynamicMaximumImmediately() {
        PersistenceData persistenceData = this.createPersistenceData();
        persistenceData.lastMagnification.setValue(1000.0F);
        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 4.0F, true);

        zoomLevelState.adjustMagnification(-1.0D, 2.0D, 800.0D);

        assertEquals(400.0D, zoomLevelState.getTargetMagnification(800.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void resettingActiveMagnificationDoesNotOverwritePersistence() {
        PersistenceData persistenceData = this.createPersistenceData();
        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 4.0F, false);
        zoomLevelState.adjustMagnification(1.0D, 1.5D, 1000.0D);

        zoomLevelState.resetTargetMagnification();

        assertEquals(4.0D, zoomLevelState.getTargetMagnification(1000.0D), DOUBLE_TOLERANCE);
        PersistenceData reloadedData = this.createPersistenceData();
        assertEquals(6.0F, reloadedData.lastMagnification.getValueOrDefault(4.0F));
    }

    @Test
    void nonSmoothZoomTransitionsImmediatelyInBothDirections() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);

        zoomLevelState.tick(true, false, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        assertEquals(4.0D, zoomLevelState.getRenderedMagnification(true, false, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(false, false, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        assertEquals(1.0D, zoomLevelState.getRenderedMagnification(false, false, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void startingAndStoppingZoomUseTheirIndependentRealTimeDurations() {
        PersistenceData persistenceData = this.createPersistenceData();
        persistenceData.lastMagnification.setValue(800.0F);
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 4.0F, true, clock);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 2.0F, 1000.0D);
        clock.advanceMilliseconds(250L);
        assertEquals(Math.pow(800.0D, 0.25D), zoomLevelState.getRenderedMagnification(true, true, 1.0F, 2.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        clock.advanceMilliseconds(750L);
        assertEquals(800.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 2.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(false, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 2.0F, 1000.0D);
        clock.advanceMilliseconds(500L);
        assertEquals(Math.pow(800.0D, 0.75D), zoomLevelState.getRenderedMagnification(false, true, 1.0F, 2.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        clock.advanceMilliseconds(1500L);
        assertEquals(ZoomMath.MIN_MAGNIFICATION, zoomLevelState.getRenderedMagnification(false, true, 1.0F, 2.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void zeroSecondDurationsTransitionImmediately() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false, clock);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 0.0F, 0.0F, 1000.0D);
        assertEquals(4.0D, zoomLevelState.getRenderedMagnification(true, true, 0.0F, 0.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(false, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 0.0F, 0.0F, 1000.0D);
        assertEquals(ZoomMath.MIN_MAGNIFICATION, zoomLevelState.getRenderedMagnification(false, true, 0.0F, 0.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void extraClientTicksDoNotAccelerateTheTransition() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false, clock);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        for (int tick = 0; tick < 20; tick++) {
            zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        }

        assertEquals(ZoomMath.MIN_MAGNIFICATION, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        clock.advanceMilliseconds(500L);
        assertEquals(2.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void aDelayedRenderCatchesUpToElapsedRealTime() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false, clock);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(1500L);

        assertEquals(4.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void exposesTheSameRealTimeProgressUsedByRenderedZoom() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false, clock);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(350L);
        zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D);

        assertEquals(0.35D, zoomLevelState.getToggleTransitionProgress(), DOUBLE_TOLERANCE);
    }

    @Test
    void smoothWheelChangesKeepTheirOriginalLogarithmicRate() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false, clock);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(1000L);
        zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D);
        zoomLevelState.adjustMagnification(2.0D, 2.0D, 1000.0D);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        assertEquals(4.8D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        assertEquals(5.76D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void smoothZoomScrollSpeedScalesTheWheelFollowerLogarithmically() {
        ZoomLevelState slowState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);
        slowState.tick(true, true, 0.5F, 0.0F, 0.0F, 1000.0D);
        slowState.adjustMagnification(3.0D, 2.0D, 1000.0D);

        slowState.tick(true, true, 0.5F, 0.0F, 0.0F, 1000.0D);

        assertEquals(4.0D * Math.pow(ZoomLevelState.WHEEL_MAGNIFICATION_CHANGE_PER_TICK, 0.5D), slowState.getRenderedMagnification(true, true, 0.0F, 0.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        ZoomLevelState fastState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);
        fastState.tick(true, true, 2.0F, 0.0F, 0.0F, 1000.0D);
        fastState.adjustMagnification(3.0D, 2.0D, 1000.0D);

        fastState.tick(true, true, 2.0F, 0.0F, 0.0F, 1000.0D);

        assertEquals(4.0D * Math.pow(ZoomLevelState.WHEEL_MAGNIFICATION_CHANGE_PER_TICK, 2.0D), fastState.getRenderedMagnification(true, true, 0.0F, 0.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void smoothZoomScrollSpeedDoesNotChangeStartOrStopAnimationProgress() {
        TestNanoClock slowClock = new TestNanoClock();
        ZoomLevelState slowState = new ZoomLevelState(this.createPersistenceData(), 16.0F, false, slowClock);
        TestNanoClock fastClock = new TestNanoClock();
        ZoomLevelState fastState = new ZoomLevelState(this.createPersistenceData(), 16.0F, false, fastClock);

        slowState.tick(true, true, Options.MIN_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 2.0F, 1000.0D);
        fastState.tick(true, true, Options.MAX_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 2.0F, 1000.0D);
        slowClock.advanceMilliseconds(250L);
        fastClock.advanceMilliseconds(250L);

        assertEquals(2.0D, slowState.getRenderedMagnification(true, true, 1.0F, 2.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        assertEquals(2.0D, fastState.getRenderedMagnification(true, true, 1.0F, 2.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        slowState.tick(false, true, Options.MIN_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 2.0F, 1000.0D);
        fastState.tick(false, true, Options.MAX_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 2.0F, 1000.0D);
        slowClock.advanceMilliseconds(250L);
        fastClock.advanceMilliseconds(250L);

        assertEquals(Math.sqrt(2.0D), slowState.getRenderedMagnification(false, true, 1.0F, 2.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        assertEquals(Math.sqrt(2.0D), fastState.getRenderedMagnification(false, true, 1.0F, 2.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void startAndStopAnimationDurationsDoNotChangeTheWheelFollowerRate() {
        ZoomLevelState shortAnimationState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);
        shortAnimationState.tick(true, true, 2.0F, 0.0F, 0.0F, 1000.0D);
        shortAnimationState.adjustMagnification(3.0D, 2.0D, 1000.0D);
        ZoomLevelState longAnimationState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);
        longAnimationState.tick(true, true, 2.0F, 0.0F, 0.0F, 1000.0D);
        longAnimationState.adjustMagnification(3.0D, 2.0D, 1000.0D);

        shortAnimationState.tick(true, true, 2.0F, 0.1F, 0.2F, 1000.0D);
        longAnimationState.tick(true, true, 2.0F, 5.0F, 5.0F, 1000.0D);

        assertEquals(5.76D, shortAnimationState.getRenderedMagnification(true, true, 0.1F, 0.2F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        assertEquals(5.76D, longAnimationState.getRenderedMagnification(true, true, 5.0F, 5.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void releaseDuringWheelSmoothingFadesFromTheCurrentRenderedLevel() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false, clock);
        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(1000L);
        zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D);
        zoomLevelState.adjustMagnification(2.0D, 2.0D, 1000.0D);
        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);

        zoomLevelState.tick(false, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(250L);

        assertEquals(Math.pow(4.8D, 0.75D), zoomLevelState.getRenderedMagnification(false, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void rapidToggleReversalContinuesFromTheCurrentTransition() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 16.0F, false, clock);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(500L);
        assertEquals(4.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(false, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(250L);
        assertEquals(2.0D, zoomLevelState.getRenderedMagnification(false, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(250L);
        assertEquals(4.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void completedReleasePreloadsThePersistedTargetForImmediateReactivation() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false, clock);
        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(1000L);
        zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D);
        zoomLevelState.adjustMagnification(2.0D, 2.0D, 1000.0D);
        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        zoomLevelState.tick(false, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(1000L);
        zoomLevelState.getRenderedMagnification(false, true, 1.0F, 1.0F, 1.0F, 1000.0D);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(250L);

        assertEquals(2.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void smoothWheelChangesStillInterpolateGeometricallyBetweenTicks() {
        TestNanoClock clock = new TestNanoClock();
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false, clock);
        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);
        clock.advanceMilliseconds(1000L);
        zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 1.0F, 1000.0D);
        zoomLevelState.adjustMagnification(1.0D, 2.0D, 1000.0D);

        zoomLevelState.tick(true, true, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED, 1.0F, 1.0F, 1000.0D);

        assertEquals(Math.sqrt(4.0D * 4.8D), zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1.0F, 0.5F, 1000.0D), DOUBLE_TOLERANCE);
    }

    private PersistenceData createPersistenceData() {
        return new PersistenceData(this.temporaryDirectory.resolve("persistence_data.json").toFile());
    }

    private static final class TestNanoClock implements LongSupplier {

        private long nanos;

        @Override
        public long getAsLong() {
            return this.nanos;
        }

        void advanceMilliseconds(long milliseconds) {
            this.nanos += milliseconds * 1_000_000L;
        }

    }

}
