package de.keksuccino.justzoom;

public final class ZoomMath {

    public static final float MAX_FOV = 170.0F;
    public static final float MIN_FOV = 0.1F;
    public static final double MIN_MAGNIFICATION = 1.0D;
    public static final double MAX_MAGNIFICATION = calculateMaximumMagnification(MAX_FOV);
    public static final double MAX_SCROLL_MAGNIFICATION_MULTIPLIER = 10.0D;

    private ZoomMath() {
    }

    public static float clampFov(float fov) {
        if (Float.isNaN(fov)) return MIN_FOV;
        return Math.max(MIN_FOV, Math.min(MAX_FOV, fov));
    }

    public static double normalizeMagnification(double magnification, double fallback, double maximum) {
        double safeMaximum = Double.isFinite(maximum) ? Math.max(MIN_MAGNIFICATION, Math.min(MAX_MAGNIFICATION, maximum)) : MAX_MAGNIFICATION;
        double safeFallback = Double.isFinite(fallback) ? fallback : MIN_MAGNIFICATION;
        double safeMagnification = Double.isNaN(magnification) ? safeFallback : magnification;
        return Math.max(MIN_MAGNIFICATION, Math.min(safeMaximum, safeMagnification));
    }

    public static double normalizeScrollMagnificationMultiplier(double multiplier, double fallback) {
        double safeFallback = fallback > MIN_MAGNIFICATION && Double.isFinite(fallback) ? fallback : MIN_MAGNIFICATION;
        double safeMultiplier = multiplier > MIN_MAGNIFICATION && Double.isFinite(multiplier) ? multiplier : safeFallback;
        return Math.min(MAX_SCROLL_MAGNIFICATION_MULTIPLIER, safeMultiplier);
    }

    public static float calculateZoomedFov(float normalFov, double magnification) {
        float safeNormalFov = clampFov(normalFov);
        double safeMagnification = normalizeMagnification(magnification, MIN_MAGNIFICATION, calculateMaximumMagnification(safeNormalFov));
        double normalHalfAngle = Math.toRadians(safeNormalFov) * 0.5D;
        double zoomedFov = Math.toDegrees(2.0D * Math.atan(Math.tan(normalHalfAngle) / safeMagnification));
        return clampFov((float) zoomedFov);
    }

    public static double calculateMaximumMagnification(float normalFov) {
        double normalHalfAngle = Math.toRadians(clampFov(normalFov)) * 0.5D;
        double minimumHalfAngle = Math.toRadians(MIN_FOV) * 0.5D;
        return Math.max(MIN_MAGNIFICATION, Math.tan(normalHalfAngle) / Math.tan(minimumHalfAngle));
    }

    public static double calculateEffectiveMagnification(float normalFov, float zoomedFov) {
        double normalHalfAngle = Math.toRadians(clampFov(normalFov)) * 0.5D;
        double zoomedHalfAngle = Math.toRadians(clampFov(zoomedFov)) * 0.5D;
        return normalizeMagnification(Math.tan(normalHalfAngle) / Math.tan(zoomedHalfAngle), MIN_MAGNIFICATION, calculateMaximumMagnification(normalFov));
    }

    public static double applyScroll(double magnification, double scrollDelta, double stepMultiplier, double maximum) {
        double safeMagnification = normalizeMagnification(magnification, MIN_MAGNIFICATION, maximum);
        double safeStepMultiplier = normalizeScrollMagnificationMultiplier(stepMultiplier, MIN_MAGNIFICATION);
        if (!Double.isFinite(scrollDelta) || scrollDelta == 0.0D || safeStepMultiplier == MIN_MAGNIFICATION) return safeMagnification;
        double adjustedMagnification = safeMagnification * Math.pow(safeStepMultiplier, scrollDelta);
        return normalizeMagnification(adjustedMagnification, safeMagnification, maximum);
    }

    public static double moveMagnificationTowards(double current, double target, double maximumChangeMultiplier) {
        double safeCurrent = normalizeMagnification(current, MIN_MAGNIFICATION, MAX_MAGNIFICATION);
        double safeTarget = normalizeMagnification(target, MIN_MAGNIFICATION, MAX_MAGNIFICATION);
        double safeChangeMultiplier = normalizeScrollMagnificationMultiplier(maximumChangeMultiplier, MIN_MAGNIFICATION);
        if (safeCurrent == safeTarget || safeChangeMultiplier == MIN_MAGNIFICATION) return safeTarget;
        double maximumLogChange = Math.log(safeChangeMultiplier);
        double currentLog = Math.log(safeCurrent);
        double targetLog = Math.log(safeTarget);
        double logChange = Math.max(-maximumLogChange, Math.min(maximumLogChange, targetLog - currentLog));
        if (Math.abs(targetLog - currentLog) <= maximumLogChange) return safeTarget;
        return normalizeMagnification(Math.exp(currentLog + logChange), safeCurrent, MAX_MAGNIFICATION);
    }

    public static double interpolateMagnification(double start, double end, float progress) {
        double safeStart = normalizeMagnification(start, MIN_MAGNIFICATION, MAX_MAGNIFICATION);
        double safeEnd = normalizeMagnification(end, MIN_MAGNIFICATION, MAX_MAGNIFICATION);
        double safeProgress = Math.max(0.0D, Math.min(1.0D, progress));
        return Math.exp(Math.log(safeStart) + (Math.log(safeEnd) - Math.log(safeStart)) * safeProgress);
    }

    public static double calculateMouseSensitivityScale(double effectiveMagnification) {
        return 1.0D / normalizeMagnification(effectiveMagnification, MIN_MAGNIFICATION, MAX_MAGNIFICATION);
    }

}
