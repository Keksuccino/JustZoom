package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MenuTabBar.class)
public abstract class MixinMenuTabBar extends TabNavigationBar {

    protected MixinMenuTabBar(int x, int y, int width, int height, TabManager tabManager, ImmutableList<TabButton> tabButtons, ImmutableList<Tab> tabs) {
        super(x, y, width, height, tabManager, tabButtons, tabs);
    }

    /** @reason MenuTabBar's separators bypass AbstractWidget's alpha. Preserve their appearance while allowing an owning screen to fade the tab bar. */
    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void wrap_blit_in_extractWidgetRenderState_JustZoom(GuiGraphicsExtractor instance, RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        if (this.alpha >= 1.0F) {
            original.call(instance, pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
            return;
        }
        instance.blit(pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight, ARGB.white(this.alpha));
    }

}
