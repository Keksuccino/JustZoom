package de.keksuccino.justzoom;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZoomHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Minecraft MC = Minecraft.getInstance();

    public static boolean isZoomed = false;
    public static double zoomFactor = JustZoom.getConfig().getOrDefault("base_zoom_factor", 0.25D);
    public static double lerpAmount = JustZoom.getConfig().getOrDefault("lerp_amount", 0.1D);

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

            if (JustZoom.getConfig().getOrDefault("smooth_camera_on_zoom", true)) {
                if (!isZoomed) {
                    cachedSmoothCamera = MC.options.smoothCamera;
                }
                MC.options.smoothCamera = true;
            }
            isZoomed = true;

            double modifiedZoom = fov * zoomFactor;
            if (modifiedZoom < 1.0D) {
                modifiedZoom = 1.0D;
            }
            if (modifiedZoom > 170.0D) {
                modifiedZoom = 170.0D;
            }

            cachedFov = cachedFov + (modifiedZoom - cachedFov) * lerpAmount;
            if (cachedFov > cachedDefaultFov) {
                cachedFov = cachedDefaultFov;
            }
            return cachedFov;

        } else {

            if (isZoomed) {
                if (!lastFov) {
                    cachedFov = cachedFov + (cachedDefaultFov - cachedFov) * lerpAmount;
                }
                if ((Math.abs(cachedFov - cachedDefaultFov) < 1 || (cachedFov - cachedDefaultFov) >= 0) && !lastFov) {
                    if (cachedFov < cachedDefaultFov) {
                        cachedFov += 1;
                    }
                    lastFov = true;
                }
                if (lastFov) {
                    cachedFov = cachedDefaultFov;
                    if (JustZoom.getConfig().getOrDefault("smooth_camera_on_zoom", true)) MC.options.smoothCamera = cachedSmoothCamera;
                    isZoomed = false;
                    if (JustZoom.getConfig().getOrDefault("reset_zoom_factor", true)) {
                        zoomFactor = JustZoom.getConfig().getOrDefault("base_zoom_factor", 0.25D);
                    }
                    lastFov = false;
                }
                return cachedFov;
            }

        }

        cachedFov = fov;

        return fov;

    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollEvent e) {

        if (KeyBindings.keyToggleZoom.isDown()) {

            e.setCanceled(true);

            if (e.getScrollDelta() < 0) {
                if (cachedFov < 170.0D) {
                    if (JustZoom.getConfig().getOrDefault("zoom_out_cap", true) && (cachedFov >= cachedDefaultFov)) {
                        return;
                    }
                    zoomFactor += JustZoom.getConfig().getOrDefault("zoom_out_per_scroll", 0.05D);
                }
            } else if (e.getScrollDelta() > 0) {
                if (cachedFov > 1.0D) {
                    zoomFactor -= JustZoom.getConfig().getOrDefault("zoom_in_per_scroll", 0.05D);
                    if (zoomFactor < 0) zoomFactor = 0;
                }
            }

        }

    }

}
