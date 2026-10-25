package de.keksuccino.justzoom;

import de.keksuccino.justzoom.networking.ZoomRulesPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The zoom rules of the logical server, which get sent to every joining player that has Just Zoom installed.
 */
public final class ServerZoomRules {

    private static final Logger LOGGER = LogManager.getLogger();

    private static volatile ZoomRulesPayload rules = ZoomRulesPayload.DEFAULT;

    private ServerZoomRules() {
    }

    /**
     * Reads the rules from the config file and adds missing server options to it.
     * The rules only change when this runs, so edits made to the file while a server is running apply after the next server start.
     */
    public static void load() {
        load(new ServerOptions());
    }

    static void load(@NotNull ServerOptions options) {
        rules = new ZoomRulesPayload(options.allowStandaloneZoom.getValue());
    }

    public static void onServerStarting() {
        load();
        LOGGER.info("[JUST ZOOM] Standalone zoom is {} on this server.", rules.allowStandaloneZoom() ? "allowed" : "disabled");
    }

    @NotNull
    public static ZoomRulesPayload getRules() {
        return rules;
    }

}
