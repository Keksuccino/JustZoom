package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

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

        zoomLevelState.resetTargetMagnification(4.0F);

        assertEquals(4.0D, zoomLevelState.getTargetMagnification(1000.0D), DOUBLE_TOLERANCE);
        PersistenceData reloadedData = this.createPersistenceData();
        assertEquals(6.0F, reloadedData.lastMagnification.getValueOrDefault(4.0F));
    }

    @Test
    void nonSmoothZoomTransitionsImmediatelyInBothDirections() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);

        zoomLevelState.tick(true, false, 1000.0D);
        assertEquals(4.0D, zoomLevelState.getRenderedMagnification(true, false, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(false, false, 1000.0D);
        assertEquals(1.0D, zoomLevelState.getRenderedMagnification(false, false, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void smoothToggleTransitionHasAFixedFourTickDurationAtHighMagnification() {
        PersistenceData persistenceData = this.createPersistenceData();
        persistenceData.lastMagnification.setValue(800.0F);
        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 4.0F, true);

        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(Math.pow(800.0D, 0.25D), zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(Math.sqrt(800.0D), zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        zoomLevelState.tick(true, true, 1000.0D);
        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(800.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(false, true, 1000.0D);
        assertEquals(Math.pow(800.0D, 0.75D), zoomLevelState.getRenderedMagnification(false, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        zoomLevelState.tick(false, true, 1000.0D);
        zoomLevelState.tick(false, true, 1000.0D);
        zoomLevelState.tick(false, true, 1000.0D);
        assertEquals(ZoomMath.MIN_MAGNIFICATION, zoomLevelState.getRenderedMagnification(false, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void smoothWheelChangesKeepTheirOriginalLogarithmicRate() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);

        zoomLevelState.tick(true, true, 1000.0D);
        zoomLevelState.tick(true, true, 1000.0D);
        zoomLevelState.tick(true, true, 1000.0D);
        zoomLevelState.tick(true, true, 1000.0D);
        zoomLevelState.adjustMagnification(2.0D, 2.0D, 1000.0D);

        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(4.8D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(5.76D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void releaseDuringWheelSmoothingFadesFromTheCurrentRenderedLevel() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);
        for (int tick = 0; tick < ZoomLevelState.TOGGLE_TRANSITION_TICKS; tick++) zoomLevelState.tick(true, true, 1000.0D);
        zoomLevelState.adjustMagnification(2.0D, 2.0D, 1000.0D);
        zoomLevelState.tick(true, true, 1000.0D);

        zoomLevelState.tick(false, true, 1000.0D);

        assertEquals(Math.pow(4.8D, 0.75D), zoomLevelState.getRenderedMagnification(false, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void rapidToggleReversalContinuesFromTheCurrentTransition() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 16.0F, false);

        zoomLevelState.tick(true, true, 1000.0D);
        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(4.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        zoomLevelState.tick(false, true, 1000.0D);
        assertEquals(2.0D, zoomLevelState.getRenderedMagnification(false, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(4.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void completedReleasePreloadsThePersistedTargetForImmediateReactivation() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);
        for (int tick = 0; tick < ZoomLevelState.TOGGLE_TRANSITION_TICKS; tick++) zoomLevelState.tick(true, true, 1000.0D);
        zoomLevelState.adjustMagnification(2.0D, 2.0D, 1000.0D);
        zoomLevelState.tick(true, true, 1000.0D);
        for (int tick = 0; tick < ZoomLevelState.TOGGLE_TRANSITION_TICKS; tick++) zoomLevelState.tick(false, true, 1000.0D);

        zoomLevelState.tick(true, true, 1000.0D);

        assertEquals(2.0D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void smoothZoomInterpolatesGeometricallyBetweenTicks() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);

        zoomLevelState.tick(true, true, 1000.0D);

        assertEquals(Math.pow(4.0D, 0.125D), zoomLevelState.getRenderedMagnification(true, true, 0.5F, 1000.0D), DOUBLE_TOLERANCE);
    }

    private PersistenceData createPersistenceData() {
        return new PersistenceData(this.temporaryDirectory.resolve("persistence_data.json").toFile());
    }

}
