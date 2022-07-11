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
    public static final String VERSION = "1.0.1";

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

            config.registerValue("base_zoom_factor", 0.25D, "zoom", "The base zoom factor before zooming in or out.");
            config.registerValue("zoom_in_per_scroll", 0.05D, "zoom", "How much to zoom in per scroll.");
            config.registerValue("zoom_out_per_scroll", 0.05D, "zoom", "How much to zoom out per scroll.");
            //TODO übernehmen (default true)
            config.registerValue("reset_zoom_factor", true, "zoom", "If the zoom factor should reset to the base zoom factor when stop zooming.");
            //TODO übernehmen
            config.registerValue("zoom_out_cap", true, "zoom", "Caps the maximum FOV when zooming out, so you can't zoom out more than your normal FOV.");

            config.clearUnusedValues();

            config.syncConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
