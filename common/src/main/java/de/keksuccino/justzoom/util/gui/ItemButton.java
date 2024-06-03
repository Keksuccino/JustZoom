package de.keksuccino.justzoom.util.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemButton extends Button {

    @NotNull
    protected ItemStack itemStack;
    protected int itemOffsetX = 0;
    protected int itemOffsetY = 0;

    public ItemButton(int x, int y, Component message, OnPress onPress, CreateNarration narrationSupplier, @NotNull ItemStack itemStack) {
        super(x, y, 20, 20, message, onPress, narrationSupplier);
        this.itemStack = itemStack;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.renderWidget(graphics, mouseX, mouseY, partial);
        graphics.renderFakeItem(this.itemStack, this.getX() + this.itemOffsetX, this.getY() + this.itemOffsetY);
    }

    @Override
    public void renderString(@NotNull GuiGraphics graphics, @NotNull Font font, int color) {
        //empty
    }

    public ItemButton setItemPositionOffset(int x, int y) {
        this.itemOffsetX = x;
        this.itemOffsetY = y;
        return this;
    }

}
