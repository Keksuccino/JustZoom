package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import de.keksuccino.justzoom.util.GameDirectoryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public class JustZoom {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String VERSION = "2.1.0";
    public static final String LOADER = Services.PLATFORM.getPlatformName().toUpperCase();
    public static final String MOD_ID = "justzoom";
    public static final File MOD_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/config/justzoom"));

    private static Options options;

    public static void init() {

        if (Services.PLATFORM.isOnClient()) {

            LOGGER.info("[JUST ZOOM] Starting version " + VERSION + " on " + Services.PLATFORM.getPlatformDisplayName() + "..");

        } else {

            LOGGER.warn("[JUST ZOOM] Disabling 'Just Zoom' since it's a client-side mod and current environment is server!");

        }

    }

    public static void updateOptions() {
        options = new Options();
    }

    @NotNull
    public static Options getOptions() {
        if (options == null) updateOptions();
        return options;
    }

    private static File createDirectory(@NotNull File file) {
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

}
