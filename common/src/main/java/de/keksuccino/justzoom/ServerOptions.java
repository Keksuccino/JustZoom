package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.config.ConfigSection;
import de.keksuccino.justzoom.util.config.ConfigValue;
import de.keksuccino.justzoom.util.config.JsonConfig;
import org.jetbrains.annotations.NotNull;
import java.io.File;

/**
 * Options of the logical server, stored in the "server" section of the shared config file.
 * They have no options screen entry and are meant to be edited in the file. They are read on every server start and only written to add missing values.
 */
public class ServerOptions extends JsonConfig {

    private final ConfigSection server = this.section("server");

    public final ConfigValue<Boolean> allowStandaloneZoom = this.server.option("allow_standalone_zoom", true);

    public ServerOptions() {
        this(JustZoom.OPTIONS_FILE);
    }

    ServerOptions(@NotNull File file) {
        super(file);
        this.save();
    }

}
