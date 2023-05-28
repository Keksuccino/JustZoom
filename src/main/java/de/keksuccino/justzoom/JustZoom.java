package de.keksuccino.justzoom;

import de.keksuccino.konkrete.config.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.File;

@Mod("justzoom")
public class JustZoom {

    public static Config config;

    public static final File MOD_DIRECTORY = new File("config/justzoom");

    //TODO übernehmen
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

    public static void updateConfig() {

        try {

            config = new Config("config/justzoom/config.txt");

            config.registerValue("base_zoom_factor", 0.25D, "zoom", "The base zoom factor before zooming in or out. Default = 0.25");
            config.registerValue("zoom_in_per_scroll", 0.05D, "zoom", "How much to zoom in per scroll. Default = 0.05");
            config.registerValue("zoom_out_per_scroll", 0.05D, "zoom", "How much to zoom out per scroll. Default = 0.05");
            //TODO übernehmen (default true)
            config.registerValue("reset_zoom_factor", true, "zoom", "If the zoom factor should reset to the base zoom factor when stop zooming. Default = true");
            //TODO übernehmen
            config.registerValue("zoom_out_cap", true, "zoom", "Caps the maximum FOV when zooming out, so you can't zoom out more than your normal FOV. Default = true");
            config.registerValue("lerp_amount", 0.1D, "zoom", "How fast the zoom should interpolate between the current FOV and the modified/zoomed FOV. Higher values mean faster interpolation. Default = 0.1 (10 frames), Min = 0.01 (100 frames), Max = 1 (1 frame)");

            // if the lerp_amount value is above 1, set it to 1
            if (config.getOrDefault("lerp_amount", 0.1D) > 1.0D) {
                config.setValue("lerp_amount", 1.0D);
            }
            // if the lerp_amount value is below 0.01, set it to 0.01
            if (config.getOrDefault("lerp_amount", 0.1D) < 0.01D) {
                config.setValue("lerp_amount", 0.01D);
            }

            config.clearUnusedValues();

            config.syncConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
