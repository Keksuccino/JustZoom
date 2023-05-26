package de.keksuccino.justzoom;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ZoomHandler {

    private static Minecraft mc = Minecraft.getInstance();

    public static boolean isZoomed = false;
    public static double zoomFactor = JustZoom.config.getOrDefault("base_zoom_factor", 0.25D);

    private static double cachedFov;
    private static double cachedDefaultFov;
    private static boolean cachedSmoothCamera;

    public static void init() {

        KeyBindings.init();

        MinecraftForge.EVENT_BUS.register(new ZoomHandler());

    }

    /**
     * Returns the modified/zoomed FOV.
     */
    public static double handleZoom(double fov) {

        cachedDefaultFov = fov;

        if (KeyBindings.keyToggleZoom.isDown()) {

            if (!isZoomed) {
                cachedSmoothCamera = mc.options.smoothCamera;
            }
            mc.options.smoothCamera = true;
            isZoomed = true;

            double modifiedZoom = fov * zoomFactor;
            if (modifiedZoom < 1.0D) {
                modifiedZoom = 1.0D;
            }
            if (modifiedZoom > 170.0D) {
                modifiedZoom = 170.0D;
            }

            double lerpAmount = 0.1; // Adjust this value to control the speed of the zoom
            // In this modified version of the handleZoom() method, we use a lerp function to interpolate between the current field of view and the modified/zoomed field of view. We set the lerp amount to 0.1, which means that the interpolation will take 10 frames to complete. You can adjust this value to control the speed of the zoom.
            // Additionnal fix : using Universal Tween Engine to make the zoom smoother
            cachedFov = cachedFov + (modifiedZoom - cachedFov) * lerpAmount;
            return cachedFov;

        } else {

            if (isZoomed) {
                mc.options.smoothCamera = cachedSmoothCamera;
                isZoomed = false;
                if (JustZoom.config.getOrDefault("reset_zoom_factor", true)) {
                    zoomFactor = JustZoom.config.getOrDefault("base_zoom_factor", 0.25D);
                }
            }

        }

        cachedFov = fov;

    return fov;

}

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent e) {

        if (KeyBindings.keyToggleZoom.isDown()) {
            e.setCanceled(true);
            if (e.getScrollDelta() < 0) {
                if (cachedFov < 170.0D) {
                    if (JustZoom.config.getOrDefault("zoom_out_cap", true) && (cachedFov >= cachedDefaultFov)) {
                        return;
                    }
                    zoomFactor += JustZoom.config.getOrDefault("zoom_out_per_scroll", 0.05D);
                }
            } else if (e.getScrollDelta() > 0) {
                if (cachedFov > 1.0D) {
                    zoomFactor -= JustZoom.config.getOrDefault("zoom_in_per_scroll", 0.05D);
                }
            }
        }

    }

}
