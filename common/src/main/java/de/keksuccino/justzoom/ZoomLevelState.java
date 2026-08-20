package de.keksuccino.justzoom;

import de.keksuccino.justzoom.persistence.PersistenceData;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

final class ZoomLevelState {

    private final PersistenceData persistenceData;
    private float zoomModifier;

    ZoomLevelState(@NotNull PersistenceData persistenceData, float baseZoomModifier, boolean applyLastZoomLevel) {
        this.persistenceData = Objects.requireNonNull(persistenceData);
        float lastZoomLevel = persistenceData.get(PersistenceData.LAST_ZOOM_LEVEL, baseZoomModifier);
        this.zoomModifier = normalize(applyLastZoomLevel ? lastZoomLevel : baseZoomModifier, baseZoomModifier);
    }

    float getZoomModifier() {
        return this.zoomModifier;
    }

    void adjustZoomModifier(float adjustment) {
        this.zoomModifier = normalize(this.zoomModifier + adjustment, this.zoomModifier);
        this.persistenceData.set(PersistenceData.LAST_ZOOM_LEVEL, this.zoomModifier);
    }

    void resetZoomModifier(float baseZoomModifier) {
        // Resetting controls the active session only. The persisted level must remain available if applying it is enabled later.
        this.zoomModifier = normalize(baseZoomModifier, this.zoomModifier);
    }

    private static float normalize(float zoomModifier, float fallback) {
        return ZoomMath.clampFovModifier(Float.isNaN(zoomModifier) ? fallback : zoomModifier);
    }

}
