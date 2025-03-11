package de.keksuccino.justzoom;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.justzoom.util.AbstractOptions;
import de.keksuccino.justzoom.util.gui.StringWidget;
import de.keksuccino.justzoom.util.gui.TooltipHandler;
import de.keksuccino.konkrete.math.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OptionsScreen extends Screen {

    @Nullable
    protected Screen parent;

    public OptionsScreen(@Nullable Screen parent) {
        super(new TranslatableComponent("justzoom.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        TooltipHandler.clearAll();

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
        this.addRenderableWidget(new Button(centerX - 75, this.height - 40, 150, 20, CommonComponents.GUI_DONE, button -> this.onClose()));

    }

    protected Button buildToggleButton(@NotNull AbstractOptions.Option<Boolean> option, int y, @NotNull String labelBaseKey) {

        int centerX = this.width / 2;
        int buttonWidth = 200;

        Component enabled = new TranslatableComponent(labelBaseKey, new TranslatableComponent("justzoom.options.toggle.enabled").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        Component disabled = new TranslatableComponent(labelBaseKey, new TranslatableComponent("justzoom.options.toggle.disabled").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));

        Button b = new Button(centerX - (buttonWidth / 2), y, buttonWidth, 20, option.getValue() ? enabled : disabled, button -> {
            option.setValue(!option.getValue());
            button.setMessage(option.getValue() ? enabled : disabled);
        });
        TooltipHandler.putTooltip(labelBaseKey, TooltipHandler.Tooltip.widget(b, labelBaseKey + ".desc"));

        return b;

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
        Component buttonText = new TranslatableComponent(labelBaseKey, new TranslatableComponent(cornerKeys[currentValue]).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));

        Button b = new Button(centerX - (buttonWidth / 2), y, buttonWidth, 20, buttonText, button -> {
            // Cycle through corners (0-3)
            int newValue = (option.getValue() + 1) % 4;
            option.setValue(newValue);
            button.setMessage(new TranslatableComponent(labelBaseKey, new TranslatableComponent(cornerKeys[newValue]).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))));
        });
        TooltipHandler.putTooltip(labelBaseKey, TooltipHandler.Tooltip.widget(b, labelBaseKey + ".desc"));

        return b;

    }

    protected void addFloatInput(@NotNull AbstractOptions.Option<Float> option, int y, @NotNull String labelBaseKey) {

        int centerX = this.width / 2;

        StringWidget zoomOutPerScrollText = this.addRenderableWidget(new StringWidget(new TranslatableComponent(labelBaseKey), this.font));
        zoomOutPerScrollText.setX(centerX - 5 - zoomOutPerScrollText.getWidth());
        zoomOutPerScrollText.setY(y + 10 - (this.font.lineHeight / 2));
        TooltipHandler.putTooltip(labelBaseKey + "_text", TooltipHandler.Tooltip.widget(zoomOutPerScrollText, labelBaseKey + ".desc"));

        EditBox zoomOutPerScroll = this.addRenderableWidget(new EditBox(this.font, centerX + 5, y, 150, 20, new TranslatableComponent(labelBaseKey)));
        zoomOutPerScroll.setValue("" + option.getValue());
        zoomOutPerScroll.setResponder(s -> {
            if (MathUtils.isFloat(s)) {
                option.setValue(Float.parseFloat(s));
            }
        });
        TooltipHandler.putTooltip(labelBaseKey + "_input", TooltipHandler.Tooltip.widget(zoomOutPerScroll, labelBaseKey + ".desc"));

    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partial);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

}