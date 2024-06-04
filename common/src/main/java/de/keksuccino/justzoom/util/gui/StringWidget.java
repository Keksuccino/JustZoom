package de.keksuccino.justzoom.util.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringWidget extends AbstractWidget {

    @NotNull
    protected Font font;
    @Nullable
    protected Button.OnTooltip tooltip;

    public StringWidget(@NotNull Component text, @NotNull Font font) {
        this(0, 0, text, font);
    }

    public StringWidget(int x, int y, @NotNull Component text, @NotNull Font font) {
        super(x, y, 0, 0, text);
        this.font = font;
        this.height = this.font.lineHeight;
        this.width = this.font.width(text);
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        this.font.drawShadow(pose, this.getMessage(), (float)this.x, (float)this.y, -1);
        if (this.isHovered) {
            this.renderToolTip(pose, mouseX, mouseY);
        }
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void renderToolTip(@NotNull PoseStack $$0, int $$1, int $$2) {
        if (this.tooltip != null) {
            this.tooltip.onTooltip(new Button(this.x, this.y, this.width, this.height, Component.empty(), var1 -> {}), $$0, $$1, $$2);
        }
    }

    public void setTooltip(@Nullable Button.OnTooltip tooltip) {
        this.tooltip = tooltip;
    }
    
}
