package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.util.config.gui.WidgetRenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBox.class)
public abstract class MixinEditBox extends AbstractWidget {

    protected MixinEditBox(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** @reason Older EditBox renderers ignore widget alpha; make borders follow the config preview opacity. */
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void wrap_blitSprite_in_renderWidget_JustZoom(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> original) {
        WidgetRenderUtils.withAlpha(graphics, this.alpha, () -> original.call(graphics, sprite, x, y, width, height));
    }

    /** @reason Fade editable and uneditable text with the rest of the input. */
    @ModifyExpressionValue(method = "renderWidget", at = {@At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;textColor:I"), @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;textColorUneditable:I")})
    private int wrap_textColor_in_renderWidget_JustZoom(int original) {
        return WidgetRenderUtils.multiplyAlpha(original, this.alpha);
    }

    /** @reason Cursor and selection colors bypass the editable-text color fields. */
    @WrapOperation(method = {"renderWidget", "renderHighlight"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"))
    private void wrap_fill_in_renderWidget_JustZoom(GuiGraphics graphics, RenderType renderType, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        original.call(graphics, renderType, x0, y0, x1, y1, WidgetRenderUtils.multiplyAlpha(color, this.alpha));
    }

}
