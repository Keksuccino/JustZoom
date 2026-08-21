package de.keksuccino.justzoom;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.function.LongSupplier;

final class ZoomLevelState {

    static final double WHEEL_MAGNIFICATION_CHANGE_PER_TICK = 1.2D;
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0D;

    private final PersistenceData persistenceData;
    private final LongSupplier nanoTimeSource;
    private double targetMagnification;
    private double previousSmoothedActiveMagnification;
    private double smoothedActiveMagnification;
    private double toggleTransitionProgress;
    private long lastTransitionUpdateNanos;
    private boolean transitionTimeInitialized;
    private boolean transitionDirectionZooming;

    ZoomLevelState(@NotNull PersistenceData persistenceData, float baseMagnification, boolean applyLastMagnification) {
        this(persistenceData, baseMagnification, applyLastMagnification, System::nanoTime);
    }

    ZoomLevelState(@NotNull PersistenceData persistenceData, float baseMagnification, boolean applyLastMagnification, @NotNull LongSupplier nanoTimeSource) {
        this.persistenceData = Objects.requireNonNull(persistenceData);
        this.nanoTimeSource = Objects.requireNonNull(nanoTimeSource);
        double normalizedBaseMagnification = normalize(baseMagnification, Options.DEFAULT_BASE_MAGNIFICATION, ZoomMath.MAX_MAGNIFICATION);
        float lastMagnification = persistenceData.lastMagnification.getValueOrDefault((float) normalizedBaseMagnification);
        this.targetMagnification = normalize(applyLastMagnification ? lastMagnification : normalizedBaseMagnification, normalizedBaseMagnification, ZoomMath.MAX_MAGNIFICATION);
        this.previousSmoothedActiveMagnification = this.targetMagnification;
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

    void tick(boolean zooming, boolean smooth, float zoomInTransitionSpeed, float zoomOutTransitionSpeed, double maximumMagnification) {
        this.updateToggleTransition(zooming, smooth, zoomInTransitionSpeed, zoomOutTransitionSpeed);
        this.previousSmoothedActiveMagnification = normalize(this.smoothedActiveMagnification, this.getTargetMagnification(maximumMagnification), maximumMagnification);
        if (!smooth) {
            this.smoothedActiveMagnification = this.getTargetMagnification(maximumMagnification);
            this.previousSmoothedActiveMagnification = this.smoothedActiveMagnification;
            return;
        }

        double activeTarget = this.getTargetMagnification(maximumMagnification);
        this.smoothedActiveMagnification = normalize(this.smoothedActiveMagnification, activeTarget, maximumMagnification);
        if (zooming) {
            // Only wheel-driven target changes use the deliberately slower magnification follower.
            this.smoothedActiveMagnification = ZoomMath.moveMagnificationTowards(this.smoothedActiveMagnification, activeTarget, WHEEL_MAGNIFICATION_CHANGE_PER_TICK);
        }

        this.preloadActiveMagnificationIfInactive(zooming, maximumMagnification);
    }

    double getRenderedMagnification(boolean zooming, boolean smooth, float zoomInTransitionSpeed, float zoomOutTransitionSpeed, float partialTicks, double maximumMagnification) {
        double target = zooming ? this.getTargetMagnification(maximumMagnification) : ZoomMath.MIN_MAGNIFICATION;
        this.updateToggleTransition(zooming, smooth, zoomInTransitionSpeed, zoomOutTransitionSpeed);
        if (!smooth) return target;

        this.preloadActiveMagnificationIfInactive(zooming, maximumMagnification);

        double interpolatedActiveMagnification = ZoomMath.interpolateMagnification(this.previousSmoothedActiveMagnification, this.smoothedActiveMagnification, partialTicks);
        double renderedMagnification = ZoomMath.interpolateMagnification(ZoomMath.MIN_MAGNIFICATION, interpolatedActiveMagnification, (float) this.toggleTransitionProgress);
        return ZoomMath.normalizeMagnification(renderedMagnification, target, maximumMagnification);
    }

    private static double normalize(double magnification, double fallback, double maximumMagnification) {
        return ZoomMath.normalizeMagnification(magnification, fallback, maximumMagnification);
    }

    private void preloadActiveMagnificationIfInactive(boolean zooming, double maximumMagnification) {
        if (zooming || this.toggleTransitionProgress != 0.0D) return;
        // Preload an inactive persisted target invisibly so the next activation never inherits wheel-smoothing latency.
        double activeTarget = this.getTargetMagnification(maximumMagnification);
        this.smoothedActiveMagnification = activeTarget;
        this.previousSmoothedActiveMagnification = activeTarget;
    }

    private void updateToggleTransition(boolean zooming, boolean smooth, float zoomInTransitionSpeed, float zoomOutTransitionSpeed) {
        long now = this.nanoTimeSource.getAsLong();
        if (!smooth) {
            this.toggleTransitionProgress = zooming ? 1.0D : 0.0D;
            this.transitionDirectionZooming = zooming;
            this.lastTransitionUpdateNanos = now;
            this.transitionTimeInitialized = true;
            return;
        }

        if (!this.transitionTimeInitialized) {
            this.lastTransitionUpdateNanos = now;
            this.transitionTimeInitialized = true;
            this.transitionDirectionZooming = zooming;
        } else {
            // Advance the previously observed direction first; a reversal begins at this sample so it never jumps across an unknown input boundary.
            double elapsedSeconds = Math.max(0.0D, (now - this.lastTransitionUpdateNanos) / NANOSECONDS_PER_SECOND);
            float activeTransitionSpeed = transitionSpeedFor(this.transitionDirectionZooming, zoomInTransitionSpeed, zoomOutTransitionSpeed);
            this.advanceToggleTransition(this.transitionDirectionZooming, elapsedSeconds, activeTransitionSpeed);
            this.lastTransitionUpdateNanos = now;
            this.transitionDirectionZooming = zooming;
        }

        float currentTransitionSpeed = transitionSpeedFor(zooming, zoomInTransitionSpeed, zoomOutTransitionSpeed);
        if (currentTransitionSpeed == Options.MIN_TRANSITION_SPEED) {
            this.toggleTransitionProgress = zooming ? 1.0D : 0.0D;
        }
    }

    private static float transitionSpeedFor(boolean zooming, float zoomInTransitionSpeed, float zoomOutTransitionSpeed) {
        return zooming ? Options.normalizeTransitionSpeed(zoomInTransitionSpeed, Options.DEFAULT_ZOOM_IN_TRANSITION_SPEED) : Options.normalizeTransitionSpeed(zoomOutTransitionSpeed, Options.DEFAULT_ZOOM_OUT_TRANSITION_SPEED);
    }

    private void advanceToggleTransition(boolean zooming, double elapsedSeconds, float transitionSpeed) {
        if (transitionSpeed == Options.MIN_TRANSITION_SPEED) {
            this.toggleTransitionProgress = zooming ? 1.0D : 0.0D;
            return;
        }
        this.toggleTransitionProgress = moveTowards(this.toggleTransitionProgress, zooming ? 1.0D : 0.0D, elapsedSeconds / transitionSpeed);
    }

    private static double moveTowards(double current, double target, double maximumChange) {
        if (current < target) return Math.min(current + maximumChange, target);
        return Math.max(current - maximumChange, target);
    }

}
