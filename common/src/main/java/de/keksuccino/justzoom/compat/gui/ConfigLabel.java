package de.keksuccino.justzoom.compat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/** Non-interactive row label for the pre-StringWidget API. */
public final class ConfigLabel extends AbstractWidget {

    private final Font font;

    public ConfigLabel(Component message, Font font) {
        super(0, 0, font.width(message), 9, message);
        this.font = font;
    }

    @Override
    public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        if (this.alpha <= 0.0F) return;
        this.font.drawShadow(pose, this.font.plainSubstrByWidth(this.getMessage().getString(), this.width), this.x, this.y, WidgetRenderUtils.multiplyAlpha(0xFFFFFFFF, this.alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean changeFocus(boolean forward) {
        return false;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

}
