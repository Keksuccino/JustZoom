package de.keksuccino.justzoom.util.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemButton extends Button {

    @NotNull
    protected ItemStack itemStack;
    protected int itemOffsetX = 0;
    protected int itemOffsetY = 0;

    public ItemButton(int x, int y, Component message, OnPress onPress, @NotNull ItemStack itemStack) {
        super(x, y, 20, 20, message, onPress);
        this.itemStack = itemStack;
    }

    @Override
    public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int backgroundTexY = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(pose, this.x, this.y, 0, 46 + backgroundTexY * 20, this.width / 2, this.height);
        this.blit(pose, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + backgroundTexY * 20, this.width / 2, this.height);
        this.renderBg(pose, mc, mouseX, mouseY);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(pose, mouseX, mouseY);
        }
        Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(this.itemStack, this.x + this.itemOffsetX, this.y + this.itemOffsetY);
    }

    public ItemButton setItemPositionOffset(int x, int y) {
        this.itemOffsetX = x;
        this.itemOffsetY = y;
        return this;
    }

}