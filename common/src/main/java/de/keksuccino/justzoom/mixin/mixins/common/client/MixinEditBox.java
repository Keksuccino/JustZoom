package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBox.class)
public abstract class MixinEditBox extends AbstractWidget {

    protected MixinEditBox(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** @reason EditBox exposes AbstractWidget's alpha but does not apply it to its border. Honor that API so the options preview can fade every input consistently. */
    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void wrap_blitSprite_in_extractWidgetRenderState_JustZoom(GuiGraphicsExtractor instance, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (this.alpha >= 1.0F) {
            original.call(instance, pipeline, sprite, x, y, width, height);
            return;
        }
        instance.blitSprite(pipeline, sprite, x, y, width, height, ARGB.white(this.alpha));
    }

    /** @reason Apply the same widget alpha to editable text, hints, and cursors while leaving the configured RGB color intact. */
    @ModifyExpressionValue(method = "extractWidgetRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;textColor:I"))
    private int modify_textColor_in_extractWidgetRenderState_JustZoom(int original) {
        return ARGB.multiplyAlpha(original, this.alpha);
    }

    /** @reason Apply the same widget alpha to uneditable text while leaving the configured RGB color intact. */
    @ModifyExpressionValue(method = "extractWidgetRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;textColorUneditable:I"))
    private int modify_textColorUneditable_in_extractWidgetRenderState_JustZoom(int original) {
        return ARGB.multiplyAlpha(original, this.alpha);
    }

    /** @reason EditBox selection rendering also ignores AbstractWidget's alpha; reproduce its two passes with alpha-aware colors during a fade. */
    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textHighlight(IIIIZ)V"))
    private void wrap_textHighlight_in_extractWidgetRenderState_JustZoom(GuiGraphicsExtractor instance, int x0, int y0, int x1, int y1, boolean invertText, Operation<Void> original) {
        if (this.alpha >= 1.0F) {
            original.call(instance, x0, y0, x1, y1, invertText);
            return;
        }
        if (invertText) {
            instance.fill(RenderPipelines.GUI_INVERT, x0, y0, x1, y1, ARGB.white(this.alpha));
        }
        instance.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, x0, y0, x1, y1, ARGB.multiplyAlpha(0xFF0000FF, this.alpha));
    }

}
