package de.keksuccino.justzoom;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;

final class ZoomLevelState {

    static final double WHEEL_MAGNIFICATION_CHANGE_PER_TICK = 1.2D;
    static final int TOGGLE_TRANSITION_TICKS = 4;
    private static final double TOGGLE_TRANSITION_PROGRESS_PER_TICK = 1.0D / TOGGLE_TRANSITION_TICKS;

    private final PersistenceData persistenceData;
    private double targetMagnification;
    private double smoothedActiveMagnification;
    private double toggleTransitionProgress;
    private double previousRenderedMagnification = ZoomMath.MIN_MAGNIFICATION;
    private double renderedMagnification = ZoomMath.MIN_MAGNIFICATION;

    ZoomLevelState(@NotNull PersistenceData persistenceData, float baseMagnification, boolean applyLastMagnification) {
        this.persistenceData = Objects.requireNonNull(persistenceData);
        double normalizedBaseMagnification = normalize(baseMagnification, Options.DEFAULT_BASE_MAGNIFICATION, ZoomMath.MAX_MAGNIFICATION);
        float lastMagnification = persistenceData.lastMagnification.getValueOrDefault((float) normalizedBaseMagnification);
        this.targetMagnification = normalize(applyLastMagnification ? lastMagnification : normalizedBaseMagnification, normalizedBaseMagnification, ZoomMath.MAX_MAGNIFICATION);
        this.smoothedActiveMagnification = this.targetMagnification;
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
        if (!smooth) {
            this.smoothedActiveMagnification = this.getTargetMagnification(maximumMagnification);
            this.toggleTransitionProgress = zooming ? 1.0D : 0.0D;
            this.renderedMagnification = target;
            return;
        }

        double activeTarget = this.getTargetMagnification(maximumMagnification);
        this.smoothedActiveMagnification = normalize(this.smoothedActiveMagnification, activeTarget, maximumMagnification);
        if (zooming) {
            // Only wheel-driven target changes use the deliberately slower magnification follower.
            this.smoothedActiveMagnification = ZoomMath.moveMagnificationTowards(this.smoothedActiveMagnification, activeTarget, WHEEL_MAGNIFICATION_CHANGE_PER_TICK);
        }

        // The separate toggle envelope gives key activation and release a fixed duration regardless of magnification.
        this.toggleTransitionProgress = moveTowards(this.toggleTransitionProgress, zooming ? 1.0D : 0.0D, TOGGLE_TRANSITION_PROGRESS_PER_TICK);
        if (!zooming && this.toggleTransitionProgress == 0.0D) {
            // Preload an inactive persisted target invisibly so the next activation never inherits wheel-smoothing latency.
            this.smoothedActiveMagnification = activeTarget;
        }
        this.renderedMagnification = ZoomMath.interpolateMagnification(ZoomMath.MIN_MAGNIFICATION, this.smoothedActiveMagnification, (float) this.toggleTransitionProgress);
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

    private static double moveTowards(double current, double target, double maximumChange) {
        if (current < target) return Math.min(current + maximumChange, target);
        return Math.max(current - maximumChange, target);
    }

}
