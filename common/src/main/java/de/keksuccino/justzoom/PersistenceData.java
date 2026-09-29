package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.config.ConfigValue;
import de.keksuccino.justzoom.util.config.JsonConfig;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public final class PersistenceData extends JsonConfig {

    public final ConfigValue<Float> lastMagnification = this.optional("last_magnification", Float.class);
    public final ConfigValue<Boolean> openSettingsToastShown = this.optional("open_settings_toast", Boolean.class);

    public PersistenceData(@NotNull File file) {
        super(file);
    }

}
