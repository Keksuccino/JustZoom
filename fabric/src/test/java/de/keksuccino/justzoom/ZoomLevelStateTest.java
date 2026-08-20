package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZoomLevelStateTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void appliesPersistedZoomLevelWhenEnabled() {
        PersistenceData persistenceData = this.createPersistenceData();
        persistenceData.lastZoomLevel.setValue(0.4F);

        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 0.25F, true);

        assertEquals(0.4F, zoomLevelState.getZoomModifier());
    }

    @Test
    void usesBaseLevelWhenApplyingPersistedLevelIsDisabled() {
        PersistenceData persistenceData = this.createPersistenceData();
        persistenceData.lastZoomLevel.setValue(0.4F);

        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 0.25F, false);

        assertEquals(0.25F, zoomLevelState.getZoomModifier());
    }

    @Test
    void savesAdjustedLevelWhenApplyingPersistedLevelIsDisabled() {
        PersistenceData persistenceData = this.createPersistenceData();
        persistenceData.lastZoomLevel.setValue(0.6F);
        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 0.25F, false);

        zoomLevelState.adjustZoomModifier(-0.05F);

        PersistenceData reloadedData = this.createPersistenceData();
        ZoomLevelState restartedState = new ZoomLevelState(reloadedData, 0.25F, true);
        assertEquals(0.2F, restartedState.getZoomModifier());
    }

    @Test
    void resettingActiveLevelDoesNotOverwritePersistedLevel() {
        PersistenceData persistenceData = this.createPersistenceData();
        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 0.25F, false);
        zoomLevelState.adjustZoomModifier(0.15F);

        zoomLevelState.resetZoomModifier(0.25F);

        assertEquals(0.25F, zoomLevelState.getZoomModifier());
        PersistenceData reloadedData = this.createPersistenceData();
        assertEquals(0.4F, reloadedData.lastZoomLevel.getValueOrDefault(0.25F));
    }

    @Test
    void clampsAdjustedLevelBeforeSavingIt() {
        PersistenceData persistenceData = this.createPersistenceData();
        ZoomLevelState zoomLevelState = new ZoomLevelState(persistenceData, 0.25F, true);

        zoomLevelState.adjustZoomModifier(-1.0F);

        assertEquals(ZoomMath.MIN_FOV_MODIFIER, zoomLevelState.getZoomModifier());
        PersistenceData reloadedData = this.createPersistenceData();
        assertEquals(ZoomMath.MIN_FOV_MODIFIER, reloadedData.lastZoomLevel.getValueOrDefault(0.25F));
    }

    private PersistenceData createPersistenceData() {
        return new PersistenceData(this.temporaryDirectory.resolve("persistence_data.json").toFile());
    }

}
