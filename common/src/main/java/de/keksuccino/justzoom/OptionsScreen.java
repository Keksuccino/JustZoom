package de.keksuccino.justzoom;

import de.keksuccino.justzoom.util.AbstractOptions;
import de.keksuccino.konkrete.math.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

    protected static final int BUTTON_HEIGHT = 20;
    protected static final int BUTTON_ROW_GAP = 10;
    protected static final int BUTTON_ROW_MAX_WIDTH = 410;

    @Nullable
    protected Screen parent;

    public OptionsScreen(@Nullable Screen parent) {
        super(Component.translatable("justzoom.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int topY = 50;
        int spacing = 25;

        StringWidget titleWidget = this.addRenderableWidget(new StringWidget(this.getTitle(), this.font));
        titleWidget.setX(centerX - (titleWidget.getWidth() / 2));
        titleWidget.setY(20);

        int currentY = topY;

        this.addFloatInput(JustZoom.getOptions().baseZoomFactor, currentY, "justzoom.options.base_zoom_modifier");
        currentY += spacing;

        this.addFloatInput(JustZoom.getOptions().zoomInPerScroll, currentY, "justzoom.options.zoom_in_change_modifier_per_scroll");
        currentY += spacing;

        this.addFloatInput(JustZoom.getOptions().zoomOutPerScroll, currentY, "justzoom.options.zoom_out_change_modifier_per_scroll");
        currentY += spacing;

        this.addButtonRow(currentY,
                this.buildToggleButton(JustZoom.getOptions().smoothZoomInOut, "justzoom.options.smooth_zoom_in_out"),
                this.buildToggleButton(JustZoom.getOptions().smoothCameraOnZoom, "justzoom.options.smooth_camera_movement_on_zoom"));
        currentY += spacing;

        this.addButtonRow(currentY,
                this.buildToggleButton(JustZoom.getOptions().normalizeMouseSensitivityOnZoom, "justzoom.options.normalize_mouse_sensitivity_on_zoom"),
                this.buildToggleButton(JustZoom.getOptions().allowZoomInMirroredView, "justzoom.options.allow_zoom_in_mirrored_view"));
        currentY += spacing;

        this.addButtonRow(currentY,
                this.buildToggleButton(JustZoom.getOptions().hideArmsWhenZooming, "justzoom.options.hide_arms_when_zooming"),
                this.buildToggleButton(JustZoom.getOptions().resetZoomFactorOnStopZooming, "justzoom.options.reset_zoom_factor_when_stop_zooming"));
        currentY += spacing;

        this.addButtonRow(currentY, this.buildCornerButton(JustZoom.getOptions().optionsButtonCorner, "justzoom.options.options_button_corner"), null);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(centerX - 75, this.height - 40, 150, BUTTON_HEIGHT).build());

    }

    protected Button buildToggleButton(@NotNull AbstractOptions.Option<Boolean> option, @NotNull String labelBaseKey) {

        Component enabled = Component.translatable(labelBaseKey, Component.translatable("justzoom.options.toggle.enabled").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        Component disabled = Component.translatable(labelBaseKey, Component.translatable("justzoom.options.toggle.disabled").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));

        return Button.builder(option.getValue() ? enabled : disabled, button -> {
                    option.setValue(!option.getValue());
                    button.setMessage(option.getValue() ? enabled : disabled);
                }).bounds(0, 0, this.getButtonWidth(), BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable(labelBaseKey + ".desc"))).build();

    }

    protected Button buildCornerButton(@NotNull AbstractOptions.Option<Integer> option, @NotNull String labelBaseKey) {

        String[] cornerKeys = new String[] {
                "justzoom.options.corner.bottom_left",
                "justzoom.options.corner.bottom_right",
                "justzoom.options.corner.top_left",
                "justzoom.options.corner.top_right"
        };

        int currentValue = option.getValue();
        Component buttonText = Component.translatable(labelBaseKey, Component.translatable(cornerKeys[currentValue]).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));

        return Button.builder(buttonText, button -> {
                    int newValue = (option.getValue() + 1) % 4;
                    option.setValue(newValue);
                    button.setMessage(Component.translatable(labelBaseKey, Component.translatable(cornerKeys[newValue]).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))));
                }).bounds(0, 0, this.getButtonWidth(), BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable(labelBaseKey + ".desc"))).build();

    }

    protected void addButtonRow(int y, @NotNull Button leftButton, @Nullable Button rightButton) {
        int buttonWidth = this.getButtonWidth();
        int leftX = this.getLeftButtonX(buttonWidth);
        leftButton.setPosition(rightButton == null ? (this.width / 2) - (buttonWidth / 2) : leftX, y);
        this.addRenderableWidget(leftButton);

        if (rightButton != null) {
            rightButton.setPosition(leftX + buttonWidth + BUTTON_ROW_GAP, y);
            this.addRenderableWidget(rightButton);
        }
    }

    protected int getButtonWidth() {
        return (this.getButtonRowWidth() - BUTTON_ROW_GAP) / 2;
    }

    protected int getButtonRowWidth() {
        return Math.min(BUTTON_ROW_MAX_WIDTH, this.width - 40);
    }

    protected int getLeftButtonX(int buttonWidth) {
        return (this.width / 2) - buttonWidth - (BUTTON_ROW_GAP / 2);
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
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

}
