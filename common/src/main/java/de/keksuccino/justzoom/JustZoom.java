package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import de.keksuccino.justzoom.util.GameDirectoryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public class JustZoom {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String VERSION = "3.0.0";
    public static final String MOD_ID = "justzoom";
    public static final String LOADER = Services.PLATFORM.getPlatformName().toUpperCase();
    public static final File MOD_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/config/justzoom"));
    public static final File OPTIONS_FILE = new File(MOD_DIR, "config.json");
    public static final File LEGACY_OPTIONS_FILE = new File(MOD_DIR, "config.txt");
    public static final File INSTANCE_DATA_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/justzoom_instance_data"));
    public static final File INSTANCE_PERSISTENCE_DATA = new File(INSTANCE_DATA_DIR, "persistence_data.json");

    private static Options options;
    private static PersistenceData persistenceData;

    public static void init() {

        if (Services.PLATFORM.isOnClient()) {
            LOGGER.info("[JUST ZOOM] Initializing version " + VERSION + " on " + Services.PLATFORM.getPlatformDisplayName() + "..");
        } else {
            LOGGER.warn("[JUST ZOOM] Just Zoom is a CLIENT mod. It will do nothing on a server and is not supposed to be present server-side.");
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

    @NotNull
    public static PersistenceData getPersistenceData() {
        if (persistenceData == null) persistenceData = new PersistenceData(INSTANCE_PERSISTENCE_DATA);
        return persistenceData;
    }

    private static File createDirectory(@NotNull File file) {
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

}
