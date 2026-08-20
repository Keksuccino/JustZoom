package de.keksuccino.justzoom;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

public class ZoomHandler {

    private static final ZoomLevelState ZOOM_LEVEL_STATE = new ZoomLevelState(JustZoom.getPersistenceData(), JustZoom.getOptions().baseMagnification.getValue(), !JustZoom.getOptions().resetZoomFactorOnStopZooming.getValue());

    private static float cachedNormalFov = 70.0F;
    private static double cachedEffectiveMagnification = ZoomMath.MIN_MAGNIFICATION;

    public static boolean isZooming() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() != null) {
            return false;
        }
        if (minecraft.options.getCameraType().isMirrored() && !JustZoom.getOptions().allowZoomInMirroredView.getValue()) {
            return false;
        }
        boolean spyglassScoping = minecraft.player != null && minecraft.player.isScoping();
        return ZoomInput.isActive(KeyMappings.KEY_TOGGLE_ZOOM.isDown(), spyglassScoping, shouldUseJustZoomForSpyglass());
    }

    public static boolean shouldUseJustZoomForSpyglass() {
        return JustZoom.getOptions().useJustZoomForSpyglass.getValue();
    }

    public static boolean shouldShowSpyglassOverlay() {
        return JustZoom.getOptions().showSpyglassOverlay.getValue();
    }

    public static boolean shouldZoomInOutSmooth() {
        return JustZoom.getOptions().smoothZoomInOut.getValue();
    }

    public static boolean shouldHideArmsWhenZooming() {
        return isZooming() && JustZoom.getOptions().hideArmsWhenZooming.getValue();
    }

    public static void onCameraTick() {
        boolean zooming = isZooming();
        if (!zooming && JustZoom.getOptions().resetZoomFactorOnStopZooming.getValue()) {
            ZOOM_LEVEL_STATE.resetTargetMagnification(JustZoom.getOptions().baseMagnification.getValue());
        }
        ZOOM_LEVEL_STATE.tick(zooming, shouldZoomInOutSmooth(), ZoomMath.calculateMaximumMagnification(cachedNormalFov));
    }

    public static double getRenderedMagnification(float partialTicks, float normalFov) {
        return ZOOM_LEVEL_STATE.getRenderedMagnification(isZooming(), shouldZoomInOutSmooth(), partialTicks, ZoomMath.calculateMaximumMagnification(normalFov));
    }

    public static void updateRenderedFov(float normalFov, float modifiedFov) {
        cachedNormalFov = normalFov;
        cachedEffectiveMagnification = ZoomMath.calculateEffectiveMagnification(normalFov, modifiedFov);
    }

    public static double getMouseSensitivityScale() {
        double effectiveMagnification = cachedEffectiveMagnification;
        if (!shouldZoomInOutSmooth()) {
            effectiveMagnification = isZooming() ? ZOOM_LEVEL_STATE.getTargetMagnification(ZoomMath.calculateMaximumMagnification(cachedNormalFov)) : ZoomMath.MIN_MAGNIFICATION;
        }
        return ZoomMath.calculateMouseSensitivityScale(effectiveMagnification);
    }

    public static void onMouseScroll(@NotNull MouseScrollFeedback feedback, double deltaY) {

        if (isZooming()) {

            feedback.cancel = true;

            if (deltaY != 0.0D) {
                double maximumMagnification = ZoomMath.calculateMaximumMagnification(cachedNormalFov);
                double stepMultiplier = ZoomMath.normalizeScrollMagnificationMultiplier(JustZoom.getOptions().scrollMagnificationMultiplier.getValue(), Options.DEFAULT_SCROLL_MAGNIFICATION_MULTIPLIER);
                ZOOM_LEVEL_STATE.adjustMagnification(deltaY, stepMultiplier, maximumMagnification);
            }

        }

    }

    public static class MouseScrollFeedback {

        public boolean cancel = false;

    }

    static final class ZoomInput {

        private ZoomInput() {
        }

        static boolean isActive(boolean keybindDown, boolean spyglassScoping, boolean useJustZoomForSpyglass) {
            return keybindDown || spyglassScoping && useJustZoomForSpyglass;
        }

    }

}
