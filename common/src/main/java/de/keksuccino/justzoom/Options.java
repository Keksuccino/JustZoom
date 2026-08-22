package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.config.ConfigSection;
import de.keksuccino.justzoom.util.config.ConfigValue;
import de.keksuccino.justzoom.util.config.JsonConfig;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public class Options extends JsonConfig {

    public static final int DEFAULT_BASE_ZOOM_FACTOR_PERCENTAGE = 75;
    public static final float DEFAULT_SCROLL_MAGNIFICATION_MULTIPLIER = 1.5F;
    public static final float DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED = 1.0F;
    public static final float DEFAULT_START_ZOOMING_ANIMATION_SPEED = 0.30F;
    public static final float DEFAULT_STOP_ZOOMING_ANIMATION_SPEED = 0.2F;
    public static final int DEFAULT_MAXIMUM_ZOOM_FACTOR_PERCENTAGE = 100;
    public static final int MINIMUM_ZOOM_FACTOR_PERCENTAGE = 0;
    public static final int MAXIMUM_ZOOM_FACTOR_PERCENTAGE = 100;
    public static final float MIN_ANIMATION_SPEED = 0.0F;
    public static final float MAX_ANIMATION_SPEED = 5.0F;
    public static final int ANIMATION_SPEED_STEPS_PER_SECOND = 20;
    public static final float ANIMATION_SPEED_STEP = 1.0F / ANIMATION_SPEED_STEPS_PER_SECOND;
    public static final float MIN_SMOOTH_ZOOM_SCROLL_SPEED = 0.01F;
    public static final float MAX_SMOOTH_ZOOM_SCROLL_SPEED = 10.0F;
    public static final int SMOOTH_ZOOM_SCROLL_SPEED_STEPS_PER_MULTIPLIER = 100;
    public static final float SMOOTH_ZOOM_SCROLL_SPEED_STEP = 1.0F / SMOOTH_ZOOM_SCROLL_SPEED_STEPS_PER_MULTIPLIER;

    private final ConfigSection zoom = this.section("zoom");
    private final ConfigSection spyglass = this.section("spyglass");

    public final ConfigValue<Integer> baseZoomFactor = this.zoom.option("base_zoom_factor", DEFAULT_BASE_ZOOM_FACTOR_PERCENTAGE);
    public final ConfigValue<Float> scrollMagnificationMultiplier = this.zoom.option("scroll_magnification_multiplier", DEFAULT_SCROLL_MAGNIFICATION_MULTIPLIER);
    public final ConfigValue<Boolean> smoothZoomInOut = this.zoom.option("smooth_zoom_in_out", true);
    public final ConfigValue<Float> startZoomingAnimationSpeed = this.zoom.option("start_zooming_animation_speed", DEFAULT_START_ZOOMING_ANIMATION_SPEED);
    public final ConfigValue<Float> stopZoomingAnimationSpeed = this.zoom.option("stop_zooming_animation_speed", DEFAULT_STOP_ZOOMING_ANIMATION_SPEED);
    public final ConfigValue<Float> smoothZoomScrollSpeed = this.zoom.option("smooth_zoom_scroll_speed", DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED);
    public final ConfigValue<Integer> maximumZoomFactor = this.zoom.option("maximum_zoom_factor", DEFAULT_MAXIMUM_ZOOM_FACTOR_PERCENTAGE);
    public final ConfigValue<Boolean> smoothCameraOnZoom = this.zoom.option("smooth_camera_movement_on_zoom", false);
    public final ConfigValue<Boolean> normalizeMouseSensitivityOnZoom = this.zoom.option("normalize_mouse_sensitivity_on_zoom", true);
    public final ConfigValue<Boolean> improveThirdPersonZoom = this.zoom.option("improve_third_person_zoom", true);
    public final ConfigValue<Boolean> hideArmsWhenZooming = this.zoom.option("hide_arms_when_zooming", true);
    public final ConfigValue<ShowHudMode> showHud = this.zoom.option("show_hud", ShowHudMode.NEVER);
    public final ConfigValue<Boolean> resetZoomFactorOnStopZooming = this.zoom.option("reset_zoom_factor_when_stop_zooming", false);
    public final ConfigValue<Boolean> useJustZoomForSpyglass = this.spyglass.option("use_just_zoom_for_spyglass", true);
    public final ConfigValue<SpyglassOverlayMode> spyglassOverlay = this.spyglass.option("spyglass_overlay", SpyglassOverlayMode.ONLY_SPYGLASS);
    public final ConfigValue<SpyglassSoundsMode> spyglassSounds = this.spyglass.option("spyglass_sounds", SpyglassSoundsMode.SPYGLASS_AND_KEYBIND_ZOOM);

    public Options() {
        this(JustZoom.OPTIONS_FILE);
    }

    Options(@NotNull File file) {
        super(file);
        // DO NOT CLEAR/DELETE OLD CONFIG VALUES! JUST KEEP THEM IN THE FILE.
        this.save();
    }

    static float normalizeAnimationSpeed(float seconds, float fallback) {
        float safeFallback = Float.isFinite(fallback) ? Math.max(MIN_ANIMATION_SPEED, Math.min(MAX_ANIMATION_SPEED, fallback)) : DEFAULT_START_ZOOMING_ANIMATION_SPEED;
        float clampedSeconds = Math.max(MIN_ANIMATION_SPEED, Math.min(MAX_ANIMATION_SPEED, Float.isFinite(seconds) ? seconds : safeFallback));
        int step = Math.round((clampedSeconds - MIN_ANIMATION_SPEED) * ANIMATION_SPEED_STEPS_PER_SECOND);
        return MIN_ANIMATION_SPEED + step / (float) ANIMATION_SPEED_STEPS_PER_SECOND;
    }

    static float normalizeSmoothZoomScrollSpeed(float speedMultiplier, float fallback) {
        float safeFallback = Float.isFinite(fallback) ? Math.max(MIN_SMOOTH_ZOOM_SCROLL_SPEED, Math.min(MAX_SMOOTH_ZOOM_SCROLL_SPEED, fallback)) : DEFAULT_SMOOTH_ZOOM_SCROLL_SPEED;
        float clampedMultiplier = Math.max(MIN_SMOOTH_ZOOM_SCROLL_SPEED, Math.min(MAX_SMOOTH_ZOOM_SCROLL_SPEED, Float.isFinite(speedMultiplier) ? speedMultiplier : safeFallback));
        int step = Math.round(clampedMultiplier * SMOOTH_ZOOM_SCROLL_SPEED_STEPS_PER_MULTIPLIER);
        return step / (float) SMOOTH_ZOOM_SCROLL_SPEED_STEPS_PER_MULTIPLIER;
    }

    static int normalizeZoomFactorPercentage(int percentage) {
        return Math.max(MINIMUM_ZOOM_FACTOR_PERCENTAGE, Math.min(MAXIMUM_ZOOM_FACTOR_PERCENTAGE, percentage));
    }

}
