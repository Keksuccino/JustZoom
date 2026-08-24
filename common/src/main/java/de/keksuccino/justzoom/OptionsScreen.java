package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.config.ConfigScreen;
import de.keksuccino.justzoom.util.config.ConfigSlider;
import de.keksuccino.justzoom.util.config.ConfigValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.LongSupplier;

public class OptionsScreen extends ConfigScreen {

    protected static final ChatFormatting CYCLE_VALUE_COLOR = OPTION_VALUE_COLOR;
    protected static final ChatFormatting NEVER_CYCLE_VALUE_COLOR = DISABLED_OPTION_VALUE_COLOR;
    protected static final long ZOOM_PREVIEW_LINGER_NANOS = 1_000_000_000L;
    protected static final float PREVIEW_CONTROL_OPACITY = 0.2F;
    protected static final KeybindSetting ZOOM_KEYBIND = new KeybindSetting(KeyMappings.KEY_TOGGLE_ZOOM, "justzoom.options.zoom_keybind", "justzoom.options.zoom_keybind.desc");
    protected static final KeybindSetting ZOOM_IN_KEYBIND = new KeybindSetting(KeyMappings.KEY_ZOOM_IN, "justzoom.options.zoom_in_keybind", "justzoom.options.zoom_in_keybind.desc", KeyMappings::getMouseWheelKey);
    protected static final KeybindSetting ZOOM_OUT_KEYBIND = new KeybindSetting(KeyMappings.KEY_ZOOM_OUT, "justzoom.options.zoom_out_keybind", "justzoom.options.zoom_out_keybind.desc", KeyMappings::getMouseWheelKey);
    protected static final KeybindSetting OPEN_OPTIONS_KEYBIND = new KeybindSetting(KeyMappings.KEY_OPEN_OPTIONS, "justzoom.options.open_options_keybind", "justzoom.options.open_options_keybind.desc");
    protected static final List<KeybindSetting> KEYBIND_SETTINGS = List.of(ZOOM_KEYBIND, ZOOM_IN_KEYBIND, ZOOM_OUT_KEYBIND, OPEN_OPTIONS_KEYBIND);

    @Nullable
    private OptionsTab advancedTab;
    @Nullable
    private ConfigSlider<Integer> activeZoomPreviewSlider;
    @Nullable
    private ZoomPreviewTarget activeZoomPreviewTarget;
    private final ZoomPreviewTimer zoomPreviewTimer;
    private boolean zoomPreviewActive;

    public OptionsScreen(@Nullable Screen parent) {
        super(parent, Component.translatable("justzoom.options"), "justzoom.options");
        this.zoomPreviewTimer = new ZoomPreviewTimer(System::nanoTime);
    }

    public static void openFromKeybind() {
        boolean pressed = false;
        while (KeyMappings.KEY_OPEN_OPTIONS.consumeClick()) pressed = true;
        if (!pressed) return;
        OpenOptionsToastHandler.onOpenOptionsKeyPressed();
        Minecraft.getInstance().gui.setScreen(new OptionsScreen(null));
    }

    @NotNull
    @Override
    protected List<OptionsTab> buildTabs() {
        return List.of(this.buildGeneralTab(), this.buildAdvancedTab(), this.buildControlsTab());
    }

    @Override
    protected void beforeBuildTabs() {
        this.advancedTab = null;
        this.activeZoomPreviewSlider = null;
        this.activeZoomPreviewTarget = null;
        this.zoomPreviewTimer.reset();
        this.zoomPreviewActive = false;
    }

    @NotNull
    protected OptionsTab buildGeneralTab() {
        OptionsTab tab = this.createTab(Component.translatable("justzoom.options.tab.general"));
        this.addToggleOption(tab, JustZoom.getOptions().smoothZoomInOut, "justzoom.options.smooth_zoom_in_out");
        this.addToggleOption(tab, JustZoom.getOptions().smoothCameraOnZoom, "justzoom.options.smooth_camera_movement_on_zoom");
        this.addToggleOption(tab, JustZoom.getOptions().normalizeMouseSensitivityOnZoom, "justzoom.options.normalize_mouse_sensitivity_on_zoom");
        this.addToggleOption(tab, JustZoom.getOptions().improveThirdPersonZoom, "justzoom.options.improve_third_person_zoom");
        this.addToggleOption(tab, JustZoom.getOptions().useJustZoomForSpyglass, "justzoom.options.use_just_zoom_for_spyglass");
        this.addToggleOption(tab, JustZoom.getOptions().hideArmsWhenZooming, "justzoom.options.hide_arms_when_zooming");
        this.addCycleOption(tab, JustZoom.getOptions().showHud, ShowHudMode::next, this::showHudMessage, "justzoom.options.show_hud.desc");
        this.addCycleOption(tab, JustZoom.getOptions().spyglassOverlay, SpyglassOverlayMode::next, this::spyglassOverlayMessage, "justzoom.options.spyglass_overlay.desc");
        this.addCycleOption(tab, JustZoom.getOptions().spyglassSounds, SpyglassSoundsMode::next, this::spyglassSoundsMessage, "justzoom.options.spyglass_sounds.desc");
        return tab;
    }

    @NotNull
    protected OptionsTab buildAdvancedTab() {
        OptionsTab tab = this.createTab(Component.translatable("justzoom.options.tab.advanced"));
        this.advancedTab = tab;
        this.addZoomFactorSlider(tab, JustZoom.getOptions().baseZoomFactor, "justzoom.options.base_zoom_factor", ZoomPreviewTarget.BASE_ZOOM);
        this.addZoomFactorSlider(tab, JustZoom.getOptions().maximumZoomFactor, "justzoom.options.maximum_zoom_factor", ZoomPreviewTarget.MAXIMUM_ZOOM);
        this.addToggleOption(tab, JustZoom.getOptions().resetZoomFactorOnStopZooming, "justzoom.options.reset_zoom_factor_when_stop_zooming");
        this.addPercentageSliderOption(tab, JustZoom.getOptions().zoomStepSize, "justzoom.options.zoom_step_size", Options.MINIMUM_ZOOM_STEP_SIZE_PERCENTAGE, Options.MAXIMUM_ZOOM_STEP_SIZE_PERCENTAGE);
        this.addAnimationSpeedSlider(tab, JustZoom.getOptions().startZoomingAnimationSpeed, "justzoom.options.start_zooming_animation_speed");
        this.addAnimationSpeedSlider(tab, JustZoom.getOptions().stopZoomingAnimationSpeed, "justzoom.options.stop_zooming_animation_speed");
        this.addPercentageSliderOption(tab, JustZoom.getOptions().smoothZoomScrollSpeedPercentage, "justzoom.options.smooth_zoom_scroll_speed", Options.MINIMUM_SMOOTH_ZOOM_SCROLL_SPEED_PERCENTAGE, Options.MAXIMUM_SMOOTH_ZOOM_SCROLL_SPEED_PERCENTAGE);
        return tab;
    }

    @NotNull
    protected OptionsTab buildControlsTab() {
        OptionsTab tab = this.createTab(Component.translatable("justzoom.options.tab.controls"));
        for (KeybindSetting setting : KEYBIND_SETTINGS) {
            this.addKeybindOption(tab, setting);
        }
        return tab;
    }

    protected void addAnimationSpeedSlider(@NotNull OptionsTab tab, @NotNull ConfigValue<Float> option, @NotNull String labelBaseKey) {
        this.addFloatSliderOption(tab, option, Options.MIN_ANIMATION_SPEED, Options.MAX_ANIMATION_SPEED, Options.ANIMATION_SPEED_STEP, value -> {
            String seconds = String.format(Locale.ROOT, "%.2f", value);
            return Component.translatable(labelBaseKey, Component.translatable("justzoom.options.seconds", seconds));
        }, labelBaseKey + ".desc");
    }

    protected void addZoomFactorSlider(@NotNull OptionsTab tab, @NotNull ConfigValue<Integer> option, @NotNull String labelBaseKey, @NotNull ZoomPreviewTarget previewTarget) {
        this.addPercentageSliderOption(tab, option, labelBaseKey, Options.MINIMUM_ZOOM_FACTOR_PERCENTAGE, Options.MAXIMUM_ZOOM_FACTOR_PERCENTAGE, (slider, value) -> this.onZoomFactorSliderMoved(slider, previewTarget));
    }

    @NotNull
    protected Component showHudMessage(@NotNull ShowHudMode mode) {
        Component value = Component.translatable(mode.getTranslationKey()).withStyle(Style.EMPTY.withColor(showHudValueColor(mode)));
        return Component.translatable("justzoom.options.show_hud", value);
    }

    @NotNull
    static ChatFormatting showHudValueColor(@NotNull ShowHudMode mode) {
        return cycleValueColor(mode == ShowHudMode.NEVER);
    }

    @NotNull
    protected Component spyglassOverlayMessage(@NotNull SpyglassOverlayMode mode) {
        Component value = Component.translatable(mode.getTranslationKey()).withStyle(Style.EMPTY.withColor(spyglassOverlayValueColor(mode)));
        return Component.translatable("justzoom.options.spyglass_overlay", value);
    }

    @NotNull
    static ChatFormatting spyglassOverlayValueColor(@NotNull SpyglassOverlayMode mode) {
        return cycleValueColor(mode == SpyglassOverlayMode.NEVER);
    }

    @NotNull
    protected Component spyglassSoundsMessage(@NotNull SpyglassSoundsMode mode) {
        Component value = Component.translatable(mode.getTranslationKey()).withStyle(Style.EMPTY.withColor(spyglassSoundsValueColor(mode)));
        return Component.translatable("justzoom.options.spyglass_sounds", value);
    }

    @NotNull
    static ChatFormatting spyglassSoundsValueColor(@NotNull SpyglassSoundsMode mode) {
        return cycleValueColor(mode == SpyglassSoundsMode.NEVER);
    }

    @NotNull
    private static ChatFormatting cycleValueColor(boolean neverSelected) {
        return neverSelected ? NEVER_CYCLE_VALUE_COLOR : CYCLE_VALUE_COLOR;
    }

    static boolean shouldActivateZoomPreview(boolean inWorld, boolean advancedTabSelected, boolean sliderRecentlyMoved) {
        return inWorld && advancedTabSelected && sliderRecentlyMoved;
    }

    @Nullable
    ZoomPreviewTarget getActiveZoomPreviewTarget() {
        boolean previewActive = shouldActivateZoomPreview(this.minecraft != null && this.minecraft.level != null, this.isTabSelected(this.advancedTab), this.zoomPreviewTimer.isActive());
        return previewActive && this.activeZoomPreviewSlider != null ? this.activeZoomPreviewTarget : null;
    }

    private void onZoomFactorSliderMoved(@NotNull ConfigSlider<Integer> slider, @NotNull ZoomPreviewTarget previewTarget) {
        this.activeZoomPreviewSlider = slider;
        this.activeZoomPreviewTarget = previewTarget;
        this.zoomPreviewTimer.recordMovement();
        this.updateZoomPreviewState();
    }

    private void updateZoomPreviewState() {
        this.zoomPreviewActive = this.getActiveZoomPreviewTarget() != null;
        // Apply every render so widgets restored by a tab change never retain the previous tab's opacity.
        this.updatePreviewControlOpacity();
    }

    private void updatePreviewControlOpacity() {
        float opacity = this.zoomPreviewActive ? PREVIEW_CONTROL_OPACITY : 1.0F;
        for (GuiEventListener child : this.children()) {
            updatePreviewControlOpacity(child, this.activeZoomPreviewSlider, opacity);
        }
        if (this.activeZoomPreviewSlider != null) this.activeZoomPreviewSlider.setAlpha(1.0F);
    }

    static void updatePreviewControlOpacity(@NotNull GuiEventListener listener, @Nullable AbstractWidget exemptWidget, float opacity) {
        if (listener instanceof AbstractWidget widget && widget != exemptWidget) widget.setAlpha(opacity);
        if (listener instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) {
                updatePreviewControlOpacity(child, exemptWidget, opacity);
            }
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.updateZoomPreviewState();
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    protected boolean shouldRenderFooterSeparator() {
        return !this.zoomPreviewActive;
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.updateZoomPreviewState();
        if (this.zoomPreviewActive) {
            this.minecraft.gui.hud.extractDeferredSubtitles();
            return;
        }
        super.extractBackground(graphics, mouseX, mouseY, a);
    }

    @Override
    public void onClose() {
        this.zoomPreviewTimer.reset();
        this.zoomPreviewActive = false;
        this.updatePreviewControlOpacity();
        super.onClose();
    }

    enum ZoomPreviewTarget {

        BASE_ZOOM,
        MAXIMUM_ZOOM

    }

    static final class ZoomPreviewTimer {

        private final LongSupplier nanoTimeSource;
        private long lastMovementNanos;
        private boolean movementRecorded;

        ZoomPreviewTimer(@NotNull LongSupplier nanoTimeSource) {
            this.nanoTimeSource = Objects.requireNonNull(nanoTimeSource);
        }

        void recordMovement() {
            this.lastMovementNanos = this.nanoTimeSource.getAsLong();
            this.movementRecorded = true;
        }

        boolean isActive() {
            if (!this.movementRecorded) return false;
            long elapsedNanos = this.nanoTimeSource.getAsLong() - this.lastMovementNanos;
            return elapsedNanos >= 0L && elapsedNanos <= ZOOM_PREVIEW_LINGER_NANOS;
        }

        void reset() {
            this.movementRecorded = false;
        }

    }

}
