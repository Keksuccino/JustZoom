package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TabNavigationBar.class)
public abstract class MixinTabNavigationBar {

    @Shadow @Final private ImmutableList<TabButton> tabButtons;

    /** @reason TabNavigationBar's separators bypass AbstractWidget's alpha. Preserve their appearance while allowing an owning screen to fade the tab bar. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void wrap_blit_in_render_JustZoom(GuiGraphics instance, RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        if (this.tabButtons.getFirst().getAlpha() >= 1.0F) {
            original.call(instance, pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
            return;
        }
        instance.blit(pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight, ARGB.white(this.tabButtons.getFirst().getAlpha()));
    }

}
