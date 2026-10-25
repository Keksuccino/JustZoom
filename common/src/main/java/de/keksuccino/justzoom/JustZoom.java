package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import de.keksuccino.justzoom.util.GameDirectoryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public class JustZoom {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String VERSION = "3.0.2";
    public static final String MOD_ID = "justzoom";
    public static final String LOADER = Services.PLATFORM.getPlatformName().toUpperCase();
    public static final File MOD_DIR = createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/config/justzoom"));
    public static final File OPTIONS_FILE = new File(MOD_DIR, "config.json");
    // Persistence data only exists on the client, so dedicated servers never get this directory.
    @Nullable
    public static final File INSTANCE_DATA_DIR = Services.PLATFORM.isOnClient() ? createDirectory(new File(GameDirectoryUtils.getGameDirectory(), "/justzoom_instance_data")) : null;
    public static final File INSTANCE_PERSISTENCE_DATA = new File(INSTANCE_DATA_DIR, "persistence_data.json");

    private static Options options;
    private static PersistenceData persistenceData;

    public static void init() {

        LOGGER.info("[JUST ZOOM] Initializing version " + VERSION + " on " + Services.PLATFORM.getPlatformDisplayName() + "..");

        // Adds missing server options to the config file right away, so they can be edited before the first server or world starts.
        ServerZoomRules.load();

    }

    public static void updateOptions() {
        assertIsClient();
        options = new Options();
    }

    @NotNull
    public static Options getOptions() {
        assertIsClient();
        if (options == null) updateOptions();
        return options;
    }

    @NotNull
    public static PersistenceData getPersistenceData() {
        assertIsClient();
        if (persistenceData == null) persistenceData = new PersistenceData(INSTANCE_PERSISTENCE_DATA);
        return persistenceData;
    }

    private static File createDirectory(@NotNull File file) {
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    public static void assertIsClient() {
        if (!Services.PLATFORM.isOnClient()) throw new RuntimeException("Tried to access Just Zoom's client-side features on the server-side!");
    }

}
