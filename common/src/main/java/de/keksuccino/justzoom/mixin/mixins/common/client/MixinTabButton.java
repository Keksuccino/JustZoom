package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TabButton.class)
public abstract class MixinTabButton extends AbstractWidget {

    protected MixinTabButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** @reason Menu tab sprites bypass AbstractWidget's alpha. Honor the inherited value so the options preview can fade these buttons too. */
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void wrap_blitSprite_in_renderWidget_JustZoom(GuiGraphics instance, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (this.alpha >= 1.0F) {
            original.call(instance, pipeline, sprite, x, y, width, height);
            return;
        }
        instance.blitSprite(pipeline, sprite, x, y, width, height, ARGB.white(this.alpha));
    }

    /** @reason The selected tab's inset background also needs to follow the button alpha while the world preview is visible. */
    @WrapOperation(method = "renderMenuBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderMenuBackgroundTexture(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;IIFFII)V"))
    private void wrap_renderMenuBackgroundTexture_in_renderMenuBackground_JustZoom(GuiGraphics graphics, Identifier texture, int x, int y, float u, float v, int width, int height, Operation<Void> original) {
        if (this.alpha >= 1.0F) {
            original.call(graphics, texture, x, y, u, v, width, height);
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, 32, 32, ARGB.white(this.alpha));
    }

    /** @reason The selected tab underline is part of the button and must use the same preview opacity. */
    @WrapOperation(method = "renderFocusUnderline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void wrap_fill_in_renderFocusUnderline_JustZoom(GuiGraphics instance, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        original.call(instance, x0, y0, x1, y1, ARGB.multiplyAlpha(color, this.alpha));
    }

}
