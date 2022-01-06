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
            config.registerValue("reset_zoom_factor", false, "zoom", "If the zoom factor should reset to the base zoom factor when stop zooming.");

            config.clearUnusedValues();

            config.syncConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
