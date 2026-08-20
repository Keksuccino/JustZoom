package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.config.ConfigValue;
import de.keksuccino.justzoom.util.config.JsonConfig;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public final class PersistenceData extends JsonConfig {

    public final ConfigValue<Float> lastZoomLevel = this.optional("last_zoom_level", Float.class);

    public PersistenceData(@NotNull File file) {
        super(file);
    }

}
