package de.keksuccino.justzoom.util.gui;

import java.util.Objects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class StringWidget extends AbstractWidget {

    private float alignX;
    private final Font font;
    private int color = -1;

    public StringWidget(@NotNull Component message, @NotNull Font font) {
        this(0, 0, font.width(message.getVisualOrderText()), 9, message, font);
    }

    public StringWidget(int width, int height, @NotNull Component message, @NotNull Font font) {
        this(0, 0, width, height, message, font);
    }

    public StringWidget(int x, int y, int width, int height, @NotNull Component message, @NotNull Font font) {
        super(x, y, width, height, message);
        this.alignX = 0.5F;
        this.active = false;
        this.font = font;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    protected final Font getFont() {
        return this.font;
    }

    public StringWidget setColor(int color) {
        this.color = color;
        return this;
    }

    protected final int getColor() {
        return this.color;
    }

    private StringWidget horizontalAlignment(float horizontalAlignment) {
        this.alignX = horizontalAlignment;
        return this;
    }

    public StringWidget alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public StringWidget alignCenter() {
        return this.horizontalAlignment(0.5F);
    }

    public StringWidget alignRight() {
        return this.horizontalAlignment(1.0F);
    }

    @Override
    public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        Component component = this.getMessage();
        Font font = this.getFont();
        int i = this.x + Math.round(this.alignX * (float)(this.getWidth() - font.width(component)));
        int var10000 = this.y;
        int var10001 = this.getHeight();
        Objects.requireNonNull(font);
        int j = var10000 + (var10001 - 9) / 2;
        this.font.drawShadow(pose, component, i, j, this.getColor());
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

}
