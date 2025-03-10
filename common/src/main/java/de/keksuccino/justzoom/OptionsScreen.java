package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.AbstractOptions;
import de.keksuccino.konkrete.math.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OptionsScreen extends Screen {

    @Nullable
    protected Screen parent;

    public OptionsScreen(@Nullable Screen parent) {
        super(Component.translatable("justzoom.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        // Define a fixed top position for the first option
        int topY = 50; // Starting position for the first option
        int spacing = 25; // Consistent spacing between elements

        StringWidget titleWidget = this.addRenderableWidget(new StringWidget(this.getTitle(), this.font));
        titleWidget.setX(centerX - (titleWidget.getWidth() / 2));
        titleWidget.setY(20);

        int currentY = topY; // Start from the top position

        //Base Zoom Modifier
        this.addFloatInput(JustZoom.getOptions().baseZoomFactor, currentY, "justzoom.options.base_zoom_modifier");
        currentY += spacing;

        //Zoom In Per Scroll
        this.addFloatInput(JustZoom.getOptions().zoomInPerScroll, currentY, "justzoom.options.zoom_in_change_modifier_per_scroll");
        currentY += spacing;

        //Zoom Out Per Scroll
        this.addFloatInput(JustZoom.getOptions().zoomOutPerScroll, currentY, "justzoom.options.zoom_out_change_modifier_per_scroll");
        currentY += spacing;

        //Smooth Zooming
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().smoothZoomInOut, currentY, "justzoom.options.smooth_zoom_in_out"));
        currentY += spacing;

        //Smooth Camera Movement
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().smoothCameraOnZoom, currentY, "justzoom.options.smooth_camera_movement_on_zoom"));
        currentY += spacing;

        //Normalize Mouse Sensitivity
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().normalizeMouseSensitivityOnZoom, currentY, "justzoom.options.normalize_mouse_sensitivity_on_zoom"));
        currentY += spacing;

        //Allow Zoom in Mirrored View
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().allowZoomInMirroredView, currentY, "justzoom.options.allow_zoom_in_mirrored_view"));
        currentY += spacing;

        //Hide Arms When Zooming
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().hideArmsWhenZooming, currentY, "justzoom.options.hide_arms_when_zooming"));
        currentY += spacing;

        //Reset Zoom Factor When Stop Zooming
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().resetZoomFactorOnStopZooming, currentY, "justzoom.options.reset_zoom_factor_when_stop_zooming"));
        currentY += spacing;

        //Options Button Corner
        this.addRenderableWidget(this.buildCornerButton(JustZoom.getOptions().optionsButtonCorner, currentY, "justzoom.options.options_button_corner"));

        //DONE
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(centerX - 75, this.height - 40, 150, 20).build());

    }

    protected Button buildToggleButton(@NotNull AbstractOptions.Option<Boolean> option, int y, @NotNull String labelBaseKey) {

        int centerX = this.width / 2;
        int buttonWidth = 200;

        Component enabled = Component.translatable(labelBaseKey, Component.translatable("justzoom.options.toggle.enabled").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        Component disabled = Component.translatable(labelBaseKey, Component.translatable("justzoom.options.toggle.disabled").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));

        return Button.builder(option.getValue() ? enabled : disabled, button -> {
                    option.setValue(!option.getValue());
                    button.setMessage(option.getValue() ? enabled : disabled);
                }).bounds(centerX - (buttonWidth / 2), y, buttonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable(labelBaseKey + ".desc"))).build();

    }

    protected Button buildCornerButton(@NotNull AbstractOptions.Option<Integer> option, int y, @NotNull String labelBaseKey) {

        int centerX = this.width / 2;
        int buttonWidth = 200;

        String[] cornerKeys = new String[] {
                "justzoom.options.corner.bottom_left",
                "justzoom.options.corner.bottom_right",
                "justzoom.options.corner.top_left",
                "justzoom.options.corner.top_right"
        };

        int currentValue = option.getValue();
        Component buttonText = Component.translatable(labelBaseKey, Component.translatable(cornerKeys[currentValue]).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));

        return Button.builder(buttonText, button -> {
                    // Cycle through corners (0-3)
                    int newValue = (option.getValue() + 1) % 4;
                    option.setValue(newValue);
                    button.setMessage(Component.translatable(labelBaseKey, Component.translatable(cornerKeys[newValue]).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))));
                }).bounds(centerX - (buttonWidth / 2), y, buttonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable(labelBaseKey + ".desc"))).build();

    }

    protected void addFloatInput(@NotNull AbstractOptions.Option<Float> option, int y, @NotNull String labelBaseKey) {

        int centerX = this.width / 2;

        StringWidget zoomOutPerScrollText = this.addRenderableWidget(new StringWidget(Component.translatable(labelBaseKey), this.font));
        zoomOutPerScrollText.setX(centerX - 5 - zoomOutPerScrollText.getWidth());
        zoomOutPerScrollText.setY(y + 10 - (this.font.lineHeight / 2));
        zoomOutPerScrollText.setTooltip(Tooltip.create(Component.translatable(labelBaseKey + ".desc")));
        EditBox zoomOutPerScroll = this.addRenderableWidget(new EditBox(this.font, centerX + 5, y, 150, 20, Component.translatable(labelBaseKey)));
        zoomOutPerScroll.setValue("" + option.getValue());
        zoomOutPerScroll.setResponder(s -> {
            if (MathUtils.isFloat(s)) {
                option.setValue(Float.parseFloat(s));
            }
        });
        zoomOutPerScroll.setTooltip(Tooltip.create(Component.translatable(labelBaseKey + ".desc")));

    }

    @Override
    public void render(GuiGraphics $$0, int $$1, int $$2, float $$3) {
        this.renderBackground($$0);
        super.render($$0, $$1, $$2, $$3);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

}