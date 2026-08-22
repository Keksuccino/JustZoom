package de.keksuccino.justzoom;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

final class ZoomLevelState {

    static final double WHEEL_MAGNIFICATION_CHANGE_PER_TICK = 1.2D;
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0D;

    private final PersistenceData persistenceData;
    private final DoubleSupplier baseMagnificationSupplier;
    private final LongSupplier nanoTimeSource;
    private double targetMagnification;
    private double previousSmoothedActiveMagnification;
    private double smoothedActiveMagnification;
    private double toggleTransitionProgress;
    private long lastTransitionUpdateNanos;
    private boolean usingBaseMagnification;
    private boolean transitionTimeInitialized;
    private boolean transitionDirectionZooming;

    ZoomLevelState(@NotNull PersistenceData persistenceData, double baseMagnification, boolean applyLastMagnification) {
        this(persistenceData, () -> baseMagnification, applyLastMagnification, System::nanoTime);
    }

    ZoomLevelState(@NotNull PersistenceData persistenceData, double baseMagnification, boolean applyLastMagnification, @NotNull LongSupplier nanoTimeSource) {
        this(persistenceData, () -> baseMagnification, applyLastMagnification, nanoTimeSource);
    }

    ZoomLevelState(@NotNull PersistenceData persistenceData, @NotNull DoubleSupplier baseMagnificationSupplier, boolean applyLastMagnification) {
        this(persistenceData, baseMagnificationSupplier, applyLastMagnification, System::nanoTime);
    }

    ZoomLevelState(@NotNull PersistenceData persistenceData, @NotNull DoubleSupplier baseMagnificationSupplier, boolean applyLastMagnification, @NotNull LongSupplier nanoTimeSource) {
        this.persistenceData = Objects.requireNonNull(persistenceData);
        this.baseMagnificationSupplier = Objects.requireNonNull(baseMagnificationSupplier);
        this.nanoTimeSource = Objects.requireNonNull(nanoTimeSource);
        double baseMagnification = this.getBaseMagnification(ZoomMath.MAX_MAGNIFICATION);
        Float lastMagnification = persistenceData.lastMagnification.getValueOrNull();
        this.usingBaseMagnification = !applyLastMagnification || lastMagnification == null;
        this.targetMagnification = this.usingBaseMagnification ? baseMagnification : normalize(lastMagnification, baseMagnification, ZoomMath.MAX_MAGNIFICATION);
        this.previousSmoothedActiveMagnification = this.targetMagnification;
        this.smoothedActiveMagnification = this.targetMagnification;
    }

    double getTargetMagnification(double maximumMagnification) {
        double magnification = this.usingBaseMagnification ? this.getBaseMagnification(maximumMagnification) : this.targetMagnification;
        return normalize(magnification, ZoomMath.MIN_MAGNIFICATION, maximumMagnification);
    }

    double getToggleTransitionProgress() {
        return this.toggleTransitionProgress;
    }

    void adjustMagnification(double scrollDelta, double stepMultiplier, double maximumMagnification) {
        double currentMagnification = this.getTargetMagnification(maximumMagnification);
        this.targetMagnification = ZoomMath.applyScroll(currentMagnification, scrollDelta, stepMultiplier, maximumMagnification);
        this.usingBaseMagnification = false;
        this.persistenceData.lastMagnification.setValue((float) this.targetMagnification);
    }

    void resetTargetMagnification() {
        // Resetting controls the active session only. The persisted magnification must remain available if applying it is enabled later.
        this.usingBaseMagnification = true;
    }

    void tick(boolean zooming, boolean smooth, float smoothZoomScrollSpeed, float startZoomingAnimationSpeed, float stopZoomingAnimationSpeed, double maximumMagnification) {
        this.updateToggleTransition(zooming, smooth, startZoomingAnimationSpeed, stopZoomingAnimationSpeed);
        this.previousSmoothedActiveMagnification = normalize(this.smoothedActiveMagnification, this.getTargetMagnification(maximumMagnification), maximumMagnification);
        if (!smooth) {
            this.smoothedActiveMagnification = this.getTargetMagnification(maximumMagnification);
            this.previousSmoothedActiveMagnification = this.smoothedActiveMagnification;
            return;
        }

        double activeTarget = this.getTargetMagnification(maximumMagnification);
        this.smoothedActiveMagnification = normalize(this.smoothedActiveMagnification, activeTarget, maximumMagnification);
        if (zooming) {
            // Magnification is followed in logarithmic space, so exponentiating the legacy rate scales its speed while x1.0 keeps the original behavior.
            float normalizedScrollSpeed = Options.normalizeSmoothZoomScrollSpeed(smoothZoomScrollSpeed, Options.DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED);
            double changeMultiplier = Math.pow(WHEEL_MAGNIFICATION_CHANGE_PER_TICK, normalizedScrollSpeed);
            this.smoothedActiveMagnification = ZoomMath.moveMagnificationTowards(this.smoothedActiveMagnification, activeTarget, changeMultiplier);
        }

        this.preloadActiveMagnificationIfInactive(zooming, maximumMagnification);
    }

    double getRenderedMagnification(boolean zooming, boolean smooth, float startZoomingAnimationSpeed, float stopZoomingAnimationSpeed, float partialTicks, double maximumMagnification) {
        double target = zooming ? this.getTargetMagnification(maximumMagnification) : ZoomMath.MIN_MAGNIFICATION;
        this.updateToggleTransition(zooming, smooth, startZoomingAnimationSpeed, stopZoomingAnimationSpeed);
        if (!smooth) return target;

        this.preloadActiveMagnificationIfInactive(zooming, maximumMagnification);

        double interpolatedActiveMagnification = ZoomMath.interpolateMagnification(this.previousSmoothedActiveMagnification, this.smoothedActiveMagnification, partialTicks);
        double renderedMagnification = ZoomMath.interpolateMagnification(ZoomMath.MIN_MAGNIFICATION, interpolatedActiveMagnification, (float) this.toggleTransitionProgress);
        return ZoomMath.normalizeMagnification(renderedMagnification, target, maximumMagnification);
    }

    private static double normalize(double magnification, double fallback, double maximumMagnification) {
        return ZoomMath.normalizeMagnification(magnification, fallback, maximumMagnification);
    }

    private double getBaseMagnification(double maximumMagnification) {
        return normalize(this.baseMagnificationSupplier.getAsDouble(), ZoomMath.MIN_MAGNIFICATION, maximumMagnification);
    }

    private void preloadActiveMagnificationIfInactive(boolean zooming, double maximumMagnification) {
        if (zooming || this.toggleTransitionProgress != 0.0D) return;
        // Preload an inactive persisted target invisibly so the next activation never inherits wheel-smoothing latency.
        double activeTarget = this.getTargetMagnification(maximumMagnification);
        this.smoothedActiveMagnification = activeTarget;
        this.previousSmoothedActiveMagnification = activeTarget;
    }

    private void updateToggleTransition(boolean zooming, boolean smooth, float startZoomingAnimationSpeed, float stopZoomingAnimationSpeed) {
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
            float activeAnimationSpeed = animationSpeedFor(this.transitionDirectionZooming, startZoomingAnimationSpeed, stopZoomingAnimationSpeed);
            this.advanceToggleTransition(this.transitionDirectionZooming, elapsedSeconds, activeAnimationSpeed);
            this.lastTransitionUpdateNanos = now;
            this.transitionDirectionZooming = zooming;
        }

        float currentAnimationSpeed = animationSpeedFor(zooming, startZoomingAnimationSpeed, stopZoomingAnimationSpeed);
        if (currentAnimationSpeed == Options.MIN_ANIMATION_SPEED) {
            this.toggleTransitionProgress = zooming ? 1.0D : 0.0D;
        }
    }

    private static float animationSpeedFor(boolean zooming, float startZoomingAnimationSpeed, float stopZoomingAnimationSpeed) {
        return zooming ? Options.normalizeAnimationSpeed(startZoomingAnimationSpeed, Options.DEFAULT_START_ZOOMING_ANIMATION_SPEED) : Options.normalizeAnimationSpeed(stopZoomingAnimationSpeed, Options.DEFAULT_STOP_ZOOMING_ANIMATION_SPEED);
    }

    private void advanceToggleTransition(boolean zooming, double elapsedSeconds, float animationSpeed) {
        if (animationSpeed == Options.MIN_ANIMATION_SPEED) {
            this.toggleTransitionProgress = zooming ? 1.0D : 0.0D;
            return;
        }
        this.toggleTransitionProgress = moveTowards(this.toggleTransitionProgress, zooming ? 1.0D : 0.0D, elapsedSeconds / animationSpeed);
    }

    private static double moveTowards(double current, double target, double maximumChange) {
        if (current < target) return Math.min(current + maximumChange, target);
        return Math.max(current - maximumChange, target);
    }

}
