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
    private static boolean cachedSmoothCamera;

    public static void init() {

        KeyBindings.init();

        MinecraftForge.EVENT_BUS.register(new ZoomHandler());

    }

    /**
     * Returns the modified/zoomed FOV.
     */
    public static double handleZoom(double fov) {

        if (KeyBindings.keyToggleZoom.isKeyDown()) {

            if (!isZoomed) {
                cachedSmoothCamera = mc.gameSettings.smoothCamera;
            }
            mc.gameSettings.smoothCamera = true;
            isZoomed = true;

            double modifiedZoom = fov * zoomFactor;
            if (modifiedZoom < 1.0D) {
                modifiedZoom = 1.0D;
            }
            if (modifiedZoom > 170.0D) {
                modifiedZoom = 170.0D;
            }
            cachedFov = modifiedZoom;
            return (modifiedZoom);

        } else {

            if (isZoomed) {
                mc.gameSettings.smoothCamera = cachedSmoothCamera;
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
    public void onMouseScroll(InputEvent.MouseScrollEvent e) {

        if (KeyBindings.keyToggleZoom.isKeyDown()) {
            e.setCanceled(true);
            if (e.getScrollDelta() < 0) {
                if (cachedFov < 170.0D) {
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
