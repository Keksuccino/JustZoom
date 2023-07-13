package de.keksuccino.justzoom;

import de.keksuccino.konkrete.config.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import java.io.File;

@Mod("justzoom")
public class JustZoom {

    //TODO übernehmen
    private static Config config;

    public static final File MOD_DIRECTORY = new File("config/justzoom");

    public static final String VERSION = "1.0.2";

    public JustZoom() {

        if (FMLEnvironment.dist == Dist.CLIENT) {

            try {

                if (!MOD_DIRECTORY.isDirectory()) {
                    MOD_DIRECTORY.mkdirs();
                }

                updateConfig();

                ZoomHandler.init();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    //TODO übernehmen
    @Nonnull
    public static Config getConfig() {
        if (config == null) updateConfig();
        return config;
    }

    public static void updateConfig() {

        try {

            config = new Config("config/justzoom/config.txt");

            config.registerValue("base_zoom_factor", 0.25D, "zoom", "The base zoom factor before zooming in or out.");
            config.registerValue("zoom_in_per_scroll", 0.05D, "zoom", "How much to zoom in per scroll.");
            config.registerValue("zoom_out_per_scroll", 0.05D, "zoom", "How much to zoom out per scroll.");
            config.registerValue("reset_zoom_factor", true, "zoom", "If the zoom factor should reset to the base zoom factor when stop zooming. Default = true");
            config.registerValue("zoom_out_cap", true, "zoom", "Caps the maximum FOV when zooming out, so you can't zoom out more than your normal FOV. Default = true");
            config.registerValue("lerp_amount", 0.1D, "zoom", "How fast the zoom should interpolate between the current FOV and the modified/zoomed FOV. Higher values mean faster interpolation. Default = 0.1 (10 frames), Min = 0.01 (100 frames), Max = 1 (1 frame)");
            config.registerValue("smooth_camera_on_zoom", true, "zoom", "If the camera should move smoothly when zoomed.");

            config.clearUnusedValues();

            if ((config.getOrDefault("base_zoom_factor", 0.25D) > 1) || (config.getOrDefault("base_zoom_factor", 0.25D) < 0)) config.setValue("base_zoom_factor", 0.25D);
            if ((config.getOrDefault("zoom_in_per_scroll", 0.05D) > 1) || (config.getOrDefault("zoom_in_per_scroll", 0.05D) < 0)) config.setValue("zoom_in_per_scroll", 0.05D);
            if ((config.getOrDefault("zoom_out_per_scroll", 0.05D) > 1) || (config.getOrDefault("zoom_out_per_scroll", 0.05D) < 0)) config.setValue("zoom_out_per_scroll", 0.05D);
            if ((config.getOrDefault("lerp_amount", 0.1D) > 1) || (config.getOrDefault("lerp_amount", 0.1D) < 0)) config.setValue("lerp_amount", 0.1D);

            config.syncConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
