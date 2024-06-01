package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.AbstractOptions;
import de.keksuccino.konkrete.config.Config;

public class Options extends AbstractOptions {

    protected final Config config = new Config(JustZoom.MOD_DIR.getAbsolutePath().replace("\\", "/") + "/config.txt");

    public final Option<Float> baseZoomFactor = new Option<>(config, "base_zoom_modifier", 0.25F, "zoom");
    public final Option<Float> zoomInPerScroll = new Option<>(config, "zoom_in_change_modifier_per_scroll", 0.05F, "zoom");
    public final Option<Float> zoomOutPerScroll = new Option<>(config, "zoom_out_change_modifier_per_scroll", 0.05F, "zoom");
    public final Option<Boolean> smoothZoomInOut = new Option<>(config, "smooth_zoom_in_out", true, "zoom");
    public final Option<Boolean> smoothCameraOnZoom = new Option<>(config, "smooth_camera_movement_on_zoom", false, "zoom");
    public final Option<Boolean> normalizeMouseSensitivityOnZoom = new Option<>(config, "normalize_mouse_sensitivity_on_zoom", true, "zoom");

    public Options() {
        this.config.syncConfig();
        this.config.clearUnusedValues();
    }

}
