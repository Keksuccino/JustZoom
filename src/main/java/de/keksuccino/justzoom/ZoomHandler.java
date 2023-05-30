package de.keksuccino.justzoom;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ZoomHandler {

    private static Minecraft mc = Minecraft.getInstance();

    public static boolean isZoomed = false;
    public static double zoomFactor = JustZoom.config.getOrDefault("base_zoom_factor", 0.25D);
    public static double lerpAmount = JustZoom.config.getOrDefault("lerp_amount", 0.1D);

    private static double cachedFov;
    private static double cachedDefaultFov;
    private static boolean cachedSmoothCamera;
    private static boolean lastFov = false;

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

            cachedFov = cachedFov + (modifiedZoom - cachedFov) * lerpAmount;
            return cachedFov;

        } else {

            if (isZoomed) {
                if (!lastFov) {
                    cachedFov = cachedFov + (cachedDefaultFov - cachedFov) * lerpAmount;
                }
                if (Math.abs(cachedFov - cachedDefaultFov) < 1 && !lastFov) {
                    if (cachedFov < cachedDefaultFov) {
                        cachedFov += 1;
                    }
                    lastFov = true;
                }
                if (lastFov) {
                    cachedFov = cachedDefaultFov;
                    mc.options.smoothCamera = cachedSmoothCamera;
                    isZoomed = false;
                    if (JustZoom.config.getOrDefault("reset_zoom_factor", true)) {
                        zoomFactor = JustZoom.config.getOrDefault("base_zoom_factor", 0.25D);
                    }
                    lerpAmount = JustZoom.config.getOrDefault("lerp_amount", 0.1D);
                    lastFov = false;
                }
                return cachedFov;
            }
            mc.options.smoothCamera = cachedSmoothCamera;
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
