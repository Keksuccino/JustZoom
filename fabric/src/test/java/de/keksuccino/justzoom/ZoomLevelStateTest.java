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
    void smoothZoomMovesAtTheSameLogarithmicRateInBothDirections() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);

        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(1.2D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(true, true, 1000.0D);
        assertEquals(1.44D, zoomLevelState.getRenderedMagnification(true, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);

        zoomLevelState.tick(false, true, 1000.0D);
        assertEquals(1.2D, zoomLevelState.getRenderedMagnification(false, true, 1.0F, 1000.0D), DOUBLE_TOLERANCE);
    }

    @Test
    void smoothZoomInterpolatesGeometricallyBetweenTicks() {
        ZoomLevelState zoomLevelState = new ZoomLevelState(this.createPersistenceData(), 4.0F, false);

        zoomLevelState.tick(true, true, 1000.0D);

        assertEquals(Math.sqrt(1.2D), zoomLevelState.getRenderedMagnification(true, true, 0.5F, 1000.0D), DOUBLE_TOLERANCE);
    }

    private PersistenceData createPersistenceData() {
        return new PersistenceData(this.temporaryDirectory.resolve("persistence_data.json").toFile());
    }

}
