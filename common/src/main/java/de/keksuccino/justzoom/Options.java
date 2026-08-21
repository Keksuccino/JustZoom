package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.config.ConfigSection;
import de.keksuccino.justzoom.util.config.ConfigValue;
import de.keksuccino.justzoom.util.config.JsonConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public class Options extends JsonConfig {

    public static final float DEFAULT_BASE_MAGNIFICATION = 4.0F;
    public static final float DEFAULT_SCROLL_MAGNIFICATION_MULTIPLIER = 1.5F;

    private final ConfigSection zoom = this.section("zoom");
    private final ConfigSection spyglass = this.section("spyglass");

    public final ConfigValue<Float> baseMagnification = this.zoom.option("base_magnification", DEFAULT_BASE_MAGNIFICATION);
    public final ConfigValue<Float> scrollMagnificationMultiplier = this.zoom.option("scroll_magnification_multiplier", DEFAULT_SCROLL_MAGNIFICATION_MULTIPLIER);
    public final ConfigValue<Boolean> smoothZoomInOut = this.zoom.option("smooth_zoom_in_out", true);
    public final ConfigValue<Boolean> smoothCameraOnZoom = this.zoom.option("smooth_camera_movement_on_zoom", false);
    public final ConfigValue<Boolean> normalizeMouseSensitivityOnZoom = this.zoom.option("normalize_mouse_sensitivity_on_zoom", true);
    public final ConfigValue<Boolean> allowZoomInMirroredView = this.zoom.option("allow_zoom_in_mirrored_view", false);
    public final ConfigValue<Boolean> hideArmsWhenZooming = this.zoom.option("hide_arms_when_zooming", true);
    public final ConfigValue<ShowHudMode> showHud = this.zoom.option("show_hud", ShowHudMode.NEVER);
    public final ConfigValue<Boolean> resetZoomFactorOnStopZooming = this.zoom.option("reset_zoom_factor_when_stop_zooming", false);
    public final ConfigValue<Boolean> useJustZoomForSpyglass = this.spyglass.option("use_just_zoom_for_spyglass", true);
    public final ConfigValue<SpyglassOverlayMode> spyglassOverlay = this.spyglass.option("spyglass_overlay", SpyglassOverlayMode.ONLY_SPYGLASS);
    public final ConfigValue<SpyglassSoundsMode> spyglassSounds = this.spyglass.option("spyglass_sounds", SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM);

    public Options() {
        this(JustZoom.OPTIONS_FILE, JustZoom.LEGACY_OPTIONS_FILE);
    }

    Options(@NotNull File file, @Nullable File legacyFile) {
        super(file, legacyFile);
        this.save();
    }

}
