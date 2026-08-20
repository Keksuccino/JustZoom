package de.keksuccino.justzoom;

public final class ZoomMath {

    public static final float MAX_FOV = 170.0F;
    public static final float MAX_FOV_MODIFIER = 1.0F;
    public static final float MIN_FOV = 1.0F;
    public static final float MIN_FOV_MODIFIER = 0.0000000001F;
    public static final float VANILLA_MIN_FOV_MODIFIER = 0.1F;

    private ZoomMath() {
    }

    public static float clampFov(float fov) {
        return Math.max(MIN_FOV, Math.min(MAX_FOV, fov));
    }

    public static float clampFovModifier(float modifier) {
        return Math.max(MIN_FOV_MODIFIER, Math.min(MAX_FOV_MODIFIER, modifier));
    }

    public static float calculateZoomedFov(float normalFov, float modifier) {
        return clampFov(clampFov(normalFov) * clampFovModifier(modifier));
    }

    public static float selectFovModifierMinimum(boolean expandedZoomRange, float vanillaMinimum) {
        return expandedZoomRange ? MIN_FOV_MODIFIER : vanillaMinimum;
    }

    public static boolean shouldClampFinalFov(boolean zooming, float oldModifier, float currentModifier) {
        return zooming || oldModifier < VANILLA_MIN_FOV_MODIFIER || currentModifier < VANILLA_MIN_FOV_MODIFIER;
    }

}
