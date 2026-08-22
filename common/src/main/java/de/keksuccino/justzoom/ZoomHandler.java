package de.keksuccino.justzoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZoomHandler {

    private static final float SPYGLASS_OVERLAY_INITIAL_SCALE = 0.5F;
    private static final float SPYGLASS_OVERLAY_FINAL_SCALE = 1.125F;

    private static float cachedNormalFov = 70.0F;
    private static double cachedEffectiveMagnification = ZoomMath.MIN_MAGNIFICATION;

    public static boolean isZooming() {
        Minecraft minecraft = Minecraft.getInstance();
        if (getActiveZoomPreviewTarget(minecraft) != null) return true;
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

    public static float getSpyglassOverlayScale(boolean useJustZoomAnimation, float vanillaScale) {
        return calculateSpyglassOverlayScale(useJustZoomAnimation, getZoomLevelState().getToggleTransitionProgress(), vanillaScale);
    }

    static float calculateSpyglassOverlayScale(boolean useJustZoomAnimation, double transitionProgress, float vanillaScale) {
        if (!useJustZoomAnimation) return vanillaScale;
        double normalizedProgress = Double.isNaN(transitionProgress) ? 0.0D : Math.max(0.0D, Math.min(1.0D, transitionProgress));
        return (float) (SPYGLASS_OVERLAY_INITIAL_SCALE + (SPYGLASS_OVERLAY_FINAL_SCALE - SPYGLASS_OVERLAY_INITIAL_SCALE) * normalizedProgress);
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
        if (getActiveZoomPreviewTarget(Minecraft.getInstance()) != null) return;
        boolean zooming = isZooming();
        Options options = JustZoom.getOptions();
        if (!zooming && options.resetZoomFactorOnStopZooming.getValue()) {
            getZoomLevelState().resetTargetMagnification();
        }
        getZoomLevelState().tick(zooming, options.smoothZoomInOut.getValue(), options.startZoomingAnimationSpeed.getValue(), options.stopZoomingAnimationSpeed.getValue(), getConfiguredMaximumMagnification(cachedNormalFov));
    }

    public static void onInputTick() {
        int zoomInClicks = consumeClicks(KeyMappings.KEY_ZOOM_IN);
        int zoomOutClicks = consumeClicks(KeyMappings.KEY_ZOOM_OUT);
        if (getActiveZoomPreviewTarget(Minecraft.getInstance()) != null) return;
        if (isZooming()) {
            adjustMagnification(ZoomInput.calculateKeyAdjustment(zoomInClicks, zoomOutClicks));
        }
    }

    public static double getRenderedMagnification(float partialTicks, float normalFov) {
        cachedNormalFov = normalFov;
        Options options = JustZoom.getOptions();
        double maximumMagnification = getConfiguredMaximumMagnification(normalFov);
        OptionsScreen.ZoomPreviewTarget previewTarget = getActiveZoomPreviewTarget(Minecraft.getInstance());
        if (previewTarget != null) return calculateZoomPreviewMagnification(normalFov, options.baseZoomFactor.getValue(), options.maximumZoomFactor.getValue(), previewTarget);
        return getZoomLevelState().getRenderedMagnification(isZooming(), options.smoothZoomInOut.getValue(), options.startZoomingAnimationSpeed.getValue(), options.stopZoomingAnimationSpeed.getValue(), partialTicks, maximumMagnification);
    }

    public static void updateRenderedFov(float normalFov, float modifiedFov) {
        cachedNormalFov = normalFov;
        cachedEffectiveMagnification = ZoomMath.calculateEffectiveMagnification(normalFov, modifiedFov);
    }

    public static double getMouseSensitivityScale() {
        double effectiveMagnification = cachedEffectiveMagnification;
        OptionsScreen.ZoomPreviewTarget previewTarget = getActiveZoomPreviewTarget(Minecraft.getInstance());
        if (previewTarget != null) {
            Options options = JustZoom.getOptions();
            effectiveMagnification = calculateZoomPreviewMagnification(cachedNormalFov, options.baseZoomFactor.getValue(), options.maximumZoomFactor.getValue(), previewTarget);
        } else if (!shouldZoomInOutSmooth()) {
            effectiveMagnification = isZooming() ? getZoomLevelState().getTargetMagnification(getConfiguredMaximumMagnification(cachedNormalFov)) : ZoomMath.MIN_MAGNIFICATION;
        }
        return ZoomMath.calculateMouseSensitivityScale(effectiveMagnification);
    }

    public static void onMouseScroll(@NotNull MouseScrollFeedback feedback, double deltaY) {

        if (getActiveZoomPreviewTarget(Minecraft.getInstance()) != null) return;
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
        double maximumMagnification = getConfiguredMaximumMagnification(cachedNormalFov);
        double stepMultiplier = ZoomMath.normalizeScrollMagnificationMultiplier(JustZoom.getOptions().scrollMagnificationMultiplier.getValue(), Options.DEFAULT_SCROLL_MAGNIFICATION_MULTIPLIER);
        getZoomLevelState().adjustMagnification(adjustment, stepMultiplier, maximumMagnification);
    }

    private static boolean isZoomAvailable(@NotNull Minecraft minecraft) {
        return isZoomAvailable(minecraft.gui.screen() != null);
    }

    private static double getConfiguredMaximumMagnification(float normalFov) {
        return ZoomMath.calculateMagnification(normalFov, JustZoom.getOptions().maximumZoomFactor.getValue());
    }

    private static double getConfiguredBaseMagnification(float normalFov) {
        Options options = JustZoom.getOptions();
        return calculateZoomPreviewMagnification(normalFov, options.baseZoomFactor.getValue(), options.maximumZoomFactor.getValue(), OptionsScreen.ZoomPreviewTarget.BASE_ZOOM);
    }

    static double calculateZoomPreviewMagnification(float normalFov, int baseZoomFactorPercentage, int maximumZoomFactorPercentage, @NotNull OptionsScreen.ZoomPreviewTarget previewTarget) {
        double maximumMagnification = ZoomMath.calculateMagnification(normalFov, maximumZoomFactorPercentage);
        if (previewTarget == OptionsScreen.ZoomPreviewTarget.MAXIMUM_ZOOM) return maximumMagnification;
        return Math.min(ZoomMath.calculateMagnification(normalFov, baseZoomFactorPercentage), maximumMagnification);
    }

    @Nullable
    private static OptionsScreen.ZoomPreviewTarget getActiveZoomPreviewTarget(@NotNull Minecraft minecraft) {
        return minecraft.gui.screen() instanceof OptionsScreen optionsScreen ? optionsScreen.getActiveZoomPreviewTarget() : null;
    }

    @NotNull
    private static ZoomLevelState getZoomLevelState() {
        // Delay client filesystem/config access until zoom runtime state is actually needed. Pure calculations can then stay Minecraft-light.
        return ZoomLevelStateHolder.INSTANCE;
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

    private static final class ZoomLevelStateHolder {

        private static final ZoomLevelState INSTANCE = new ZoomLevelState(JustZoom.getPersistenceData(), () -> getConfiguredBaseMagnification(cachedNormalFov), !JustZoom.getOptions().resetZoomFactorOnStopZooming.getValue());

    }

}
