package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MenuTabBar.MenuTabButton.class)
public abstract class MixinMenuTabButton extends TabButton {

    public MixinMenuTabButton(TabManager tabManager, Tab tab, int width, int height) {
        super(tabManager, tab, width, height);
    }

    /** @reason Menu tab sprites bypass AbstractWidget's alpha. Honor the inherited value so the options preview can fade these buttons too. */
    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void wrap_blitSprite_in_extractWidgetRenderState_JustZoom(GuiGraphicsExtractor instance, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (this.alpha >= 1.0F) {
            original.call(instance, pipeline, sprite, x, y, width, height);
            return;
        }
        instance.blitSprite(pipeline, sprite, x, y, width, height, ARGB.white(this.alpha));
    }

    /** @reason The selected tab's inset background also needs to follow the button alpha while the world preview is visible. */
    @WrapOperation(method = "renderMenuBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractMenuBackgroundTexture(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIFFII)V"))
    private void wrap_extractMenuBackgroundTexture_in_renderMenuBackground_JustZoom(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v, int width, int height, Operation<Void> original) {
        if (this.alpha >= 1.0F) {
            original.call(graphics, texture, x, y, u, v, width, height);
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 32, 32, ARGB.white(this.alpha));
    }

    /** @reason The selected tab underline is part of the button and must use the same preview opacity. */
    @WrapOperation(method = "renderFocusUnderline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(IIIII)V"))
    private void wrap_fill_in_renderFocusUnderline_JustZoom(GuiGraphicsExtractor instance, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        original.call(instance, x0, y0, x1, y1, ARGB.multiplyAlpha(color, this.alpha));
    }

}
