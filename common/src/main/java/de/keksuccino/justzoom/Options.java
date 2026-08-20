package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.config.ConfigSection;
import de.keksuccino.justzoom.util.config.ConfigValue;
import de.keksuccino.justzoom.util.config.JsonConfig;

public class Options extends JsonConfig {

    private final ConfigSection zoom = this.section("zoom");
    private final ConfigSection spyglass = this.section("spyglass");

    public final ConfigValue<Float> baseZoomFactor = this.zoom.option("base_zoom_modifier", 0.25F);
    public final ConfigValue<Float> zoomInPerScroll = this.zoom.option("zoom_in_change_modifier_per_scroll", 0.05F);
    public final ConfigValue<Float> zoomOutPerScroll = this.zoom.option("zoom_out_change_modifier_per_scroll", 0.05F);
    public final ConfigValue<Boolean> smoothZoomInOut = this.zoom.option("smooth_zoom_in_out", true);
    public final ConfigValue<Boolean> smoothCameraOnZoom = this.zoom.option("smooth_camera_movement_on_zoom", false);
    public final ConfigValue<Boolean> normalizeMouseSensitivityOnZoom = this.zoom.option("normalize_mouse_sensitivity_on_zoom", true);
    public final ConfigValue<Boolean> allowZoomInMirroredView = this.zoom.option("allow_zoom_in_mirrored_view", false);
    public final ConfigValue<Boolean> hideArmsWhenZooming = this.zoom.option("hide_arms_when_zooming", false);
    public final ConfigValue<Boolean> resetZoomFactorOnStopZooming = this.zoom.option("reset_zoom_factor_when_stop_zooming", false);
    public final ConfigValue<Boolean> useJustZoomForSpyglass = this.spyglass.option("use_just_zoom_for_spyglass", true);
    public final ConfigValue<Boolean> showSpyglassOverlay = this.spyglass.option("show_spyglass_overlay", true);

    public Options() {
        super(JustZoom.OPTIONS_FILE, JustZoom.LEGACY_OPTIONS_FILE);
        this.save();
    }

}
