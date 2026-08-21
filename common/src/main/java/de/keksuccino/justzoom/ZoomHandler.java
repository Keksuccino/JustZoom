package de.keksuccino.justzoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

public class ZoomHandler {

    private static final ZoomLevelState ZOOM_LEVEL_STATE = new ZoomLevelState(JustZoom.getPersistenceData(), JustZoom.getOptions().baseMagnification.getValue(), !JustZoom.getOptions().resetZoomFactorOnStopZooming.getValue());

    private static float cachedNormalFov = 70.0F;
    private static double cachedEffectiveMagnification = ZoomMath.MIN_MAGNIFICATION;

    public static boolean isZooming() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isZoomAvailable(minecraft)) return false;
        boolean spyglassScoping = minecraft.player != null && minecraft.player.isScoping();
        return ZoomInput.isActive(KeyMappings.KEY_TOGGLE_ZOOM.isDown(), spyglassScoping, shouldUseJustZoomForSpyglass());
    }

    public static boolean isKeybindZooming() {
        Minecraft minecraft = Minecraft.getInstance();
        return isZoomAvailable(minecraft) && KeyMappings.KEY_TOGGLE_ZOOM.isDown();
    }

    public static boolean shouldUseJustZoomForSpyglass() {
        return JustZoom.getOptions().useJustZoomForSpyglass.getValue();
    }

    public static boolean shouldShowSpyglassOverlay() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean spyglassScoping = minecraft.player != null && minecraft.player.isScoping();
        return shouldShowSpyglassOverlay(spyglassScoping, isKeybindZooming());
    }

    public static boolean shouldShowSpyglassOverlay(boolean spyglassScoping, boolean keybindZooming) {
        return JustZoom.getOptions().spyglassOverlay.getValue().shouldShow(spyglassScoping, keybindZooming);
    }

    public static boolean shouldZoomInOutSmooth() {
        return JustZoom.getOptions().smoothZoomInOut.getValue();
    }

    public static boolean shouldHideArmsWhenZooming() {
        return isZooming() && JustZoom.getOptions().hideArmsWhenZooming.getValue();
    }

    public static boolean shouldUseFirstPersonCameraWhileZooming() {
        Minecraft minecraft = Minecraft.getInstance();
        return shouldUseFirstPersonCameraWhileZooming(isZooming(), JustZoom.getOptions().improveThirdPersonZoom.getValue(), minecraft.options.getCameraType().isMirrored());
    }

    static boolean shouldUseFirstPersonCameraWhileZooming(boolean zooming, boolean improveThirdPersonZoom, boolean mirrored) {
        return zooming && improveThirdPersonZoom && !mirrored;
    }

    public static boolean shouldHideHudWhileZooming() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean spyglassScoping = minecraft.player != null && minecraft.player.isScoping();
        return JustZoom.getOptions().showHud.getValue().shouldHide(spyglassScoping, isKeybindZooming());
    }

    public static boolean shouldExtractSpyglassOverlaySeparately(boolean hudOriginallyHidden, boolean hudHiddenByZoom, boolean overlayVisible) {
        return !hudOriginallyHidden && hudHiddenByZoom && overlayVisible;
    }

    public static void onCameraTick() {
        boolean zooming = isZooming();
        Options options = JustZoom.getOptions();
        if (!zooming && options.resetZoomFactorOnStopZooming.getValue()) {
            ZOOM_LEVEL_STATE.resetTargetMagnification(options.baseMagnification.getValue());
        }
        ZOOM_LEVEL_STATE.tick(zooming, options.smoothZoomInOut.getValue(), options.zoomInTransitionSpeed.getValue(), options.zoomOutTransitionSpeed.getValue(), ZoomMath.calculateMaximumMagnification(cachedNormalFov));
    }

    public static void onInputTick() {
        int zoomInClicks = consumeClicks(KeyMappings.KEY_ZOOM_IN);
        int zoomOutClicks = consumeClicks(KeyMappings.KEY_ZOOM_OUT);
        if (isZooming()) {
            adjustMagnification(ZoomInput.calculateKeyAdjustment(zoomInClicks, zoomOutClicks));
        }
    }

    public static double getRenderedMagnification(float partialTicks, float normalFov) {
        Options options = JustZoom.getOptions();
        return ZOOM_LEVEL_STATE.getRenderedMagnification(isZooming(), options.smoothZoomInOut.getValue(), options.zoomInTransitionSpeed.getValue(), options.zoomOutTransitionSpeed.getValue(), partialTicks, ZoomMath.calculateMaximumMagnification(normalFov));
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
            boolean zoomInTriggered = KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_IN, deltaY);
            boolean zoomOutTriggered = KeyMappings.matchesMouseWheel(KeyMappings.KEY_ZOOM_OUT, deltaY);
            feedback.cancel = zoomInTriggered || zoomOutTriggered;
            adjustMagnification(ZoomInput.calculateScrollAdjustment(zoomInTriggered, zoomOutTriggered, deltaY));
        }

    }

    private static int consumeClicks(@NotNull KeyMapping keyMapping) {
        int clicks = 0;
        while (keyMapping.consumeClick()) clicks++;
        return clicks;
    }

    private static void adjustMagnification(double adjustment) {
        if (adjustment == 0.0D) return;
        double maximumMagnification = ZoomMath.calculateMaximumMagnification(cachedNormalFov);
        double stepMultiplier = ZoomMath.normalizeScrollMagnificationMultiplier(JustZoom.getOptions().scrollMagnificationMultiplier.getValue(), Options.DEFAULT_SCROLL_MAGNIFICATION_MULTIPLIER);
        ZOOM_LEVEL_STATE.adjustMagnification(adjustment, stepMultiplier, maximumMagnification);
    }

    private static boolean isZoomAvailable(@NotNull Minecraft minecraft) {
        return isZoomAvailable(minecraft.gui.screen() != null);
    }

    static boolean isZoomAvailable(boolean screenOpen) {
        return !screenOpen;
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

        static int calculateKeyAdjustment(int zoomInClicks, int zoomOutClicks) {
            return Math.max(0, zoomInClicks) - Math.max(0, zoomOutClicks);
        }

        static double calculateScrollAdjustment(boolean zoomInTriggered, boolean zoomOutTriggered, double deltaY) {
            if (!Double.isFinite(deltaY) || deltaY == 0.0D || zoomInTriggered == zoomOutTriggered) return 0.0D;
            return zoomInTriggered ? Math.abs(deltaY) : -Math.abs(deltaY);
        }

    }

}
