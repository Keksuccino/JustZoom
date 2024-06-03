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

    @Nullable
    protected Screen parent;

    public OptionsScreen(@Nullable Screen parent) {
        super(Component.translatable("justzoom.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        StringWidget titleWidget = this.addRenderableWidget(new StringWidget(this.getTitle(), this.font));
        titleWidget.setX(centerX - (titleWidget.getWidth() / 2));
        titleWidget.setY(20);

        //Base Zoom Modifier
        this.addFloatInput(JustZoom.getOptions().baseZoomFactor, centerY - 72, "justzoom.options.base_zoom_modifier");

        //Zoom In Per Scroll
        this.addFloatInput(JustZoom.getOptions().zoomInPerScroll, centerY - 47, "justzoom.options.zoom_in_change_modifier_per_scroll");

        //Zoom Out Per Scroll
        this.addFloatInput(JustZoom.getOptions().zoomOutPerScroll, centerY - 22, "justzoom.options.zoom_out_change_modifier_per_scroll");

        //------------------------------------ CENTER Y

        //Smooth Zooming
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().smoothZoomInOut, centerY + 3, "justzoom.options.smooth_zoom_in_out"));

        //Smooth Camera Movement
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().smoothCameraOnZoom, centerY + 28, "justzoom.options.smooth_camera_movement_on_zoom"));

        //Normalize Mouse Sensitivity
        this.addRenderableWidget(this.buildToggleButton(JustZoom.getOptions().normalizeMouseSensitivityOnZoom, centerY + 53, "justzoom.options.normalize_mouse_sensitivity_on_zoom"));

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
