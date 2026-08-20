package de.keksuccino.justzoom;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ZoomHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    public static float zoomModifier = JustZoom.getOptions().baseZoomFactor.getValue();
    public static float cachedNormalFov = 0.0F;
    public static float cachedModifiedFov = 0.0F;

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

    /**
     * Returns the FOV modifier for zooming.
     */
    public static float getFovModifier() {

        //To not zoom out further than normal FOV
        if (zoomModifier > 1.0F) zoomModifier = 1.0F;
        //To not break FOV calculations
        if (zoomModifier <= 0.0F) zoomModifier = 0.0000000001F;

        return zoomModifier;

    }

    public static void onMouseScroll(@NotNull MouseScrollFeedback feedback, double deltaX, double deltaY) {

        if (isZooming()) {

            feedback.cancel = true;

            if (deltaY < 0) {
                zoomModifier += JustZoom.getOptions().zoomOutPerScroll.getValue();
            } else if (deltaY > 0) {
                zoomModifier -= JustZoom.getOptions().zoomInPerScroll.getValue();
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
