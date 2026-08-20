package de.keksuccino.justzoom;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;

final class ZoomLevelState {

    static final double SMOOTH_MAGNIFICATION_CHANGE_PER_TICK = 1.2D;

    private final PersistenceData persistenceData;
    private double targetMagnification;
    private double previousRenderedMagnification = ZoomMath.MIN_MAGNIFICATION;
    private double renderedMagnification = ZoomMath.MIN_MAGNIFICATION;

    ZoomLevelState(@NotNull PersistenceData persistenceData, float baseMagnification, boolean applyLastMagnification) {
        this.persistenceData = Objects.requireNonNull(persistenceData);
        double normalizedBaseMagnification = normalize(baseMagnification, Options.DEFAULT_BASE_MAGNIFICATION, ZoomMath.MAX_MAGNIFICATION);
        float lastMagnification = persistenceData.lastMagnification.getValueOrDefault((float) normalizedBaseMagnification);
        this.targetMagnification = normalize(applyLastMagnification ? lastMagnification : normalizedBaseMagnification, normalizedBaseMagnification, ZoomMath.MAX_MAGNIFICATION);
    }

    double getTargetMagnification(double maximumMagnification) {
        return normalize(this.targetMagnification, ZoomMath.MIN_MAGNIFICATION, maximumMagnification);
    }

    void adjustMagnification(double scrollDelta, double stepMultiplier, double maximumMagnification) {
        double currentMagnification = this.getTargetMagnification(maximumMagnification);
        this.targetMagnification = ZoomMath.applyScroll(currentMagnification, scrollDelta, stepMultiplier, maximumMagnification);
        this.persistenceData.lastMagnification.setValue((float) this.targetMagnification);
    }

    void resetTargetMagnification(float baseMagnification) {
        // Resetting controls the active session only. The persisted magnification must remain available if applying it is enabled later.
        this.targetMagnification = normalize(baseMagnification, Options.DEFAULT_BASE_MAGNIFICATION, ZoomMath.MAX_MAGNIFICATION);
    }

    void tick(boolean zooming, boolean smooth, double maximumMagnification) {
        this.previousRenderedMagnification = ZoomMath.normalizeMagnification(this.renderedMagnification, ZoomMath.MIN_MAGNIFICATION, maximumMagnification);
        double target = zooming ? this.getTargetMagnification(maximumMagnification) : ZoomMath.MIN_MAGNIFICATION;
        this.renderedMagnification = smooth ? ZoomMath.moveMagnificationTowards(this.previousRenderedMagnification, target, SMOOTH_MAGNIFICATION_CHANGE_PER_TICK) : target;
        this.renderedMagnification = ZoomMath.normalizeMagnification(this.renderedMagnification, target, maximumMagnification);
    }

    double getRenderedMagnification(boolean zooming, boolean smooth, float partialTicks, double maximumMagnification) {
        double target = zooming ? this.getTargetMagnification(maximumMagnification) : ZoomMath.MIN_MAGNIFICATION;
        if (!smooth) return target;
        double interpolated = ZoomMath.interpolateMagnification(this.previousRenderedMagnification, this.renderedMagnification, partialTicks);
        return ZoomMath.normalizeMagnification(interpolated, target, maximumMagnification);
    }

    private static double normalize(double magnification, double fallback, double maximumMagnification) {
        return ZoomMath.normalizeMagnification(magnification, fallback, maximumMagnification);
    }

}
