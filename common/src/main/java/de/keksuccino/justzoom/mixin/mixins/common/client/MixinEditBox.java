package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.justzoom.compat.gui.WidgetRenderUtils;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBox.class)
public abstract class MixinEditBox extends AbstractWidget {

    protected MixinEditBox(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** @reason Fade borders and cursor rectangles alongside the config preview controls. */
    @WrapOperation(method = "renderButton", at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"), @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V")})
    private void wrap_fill_in_renderButton_JustZoom(PoseStack pose, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        original.call(pose, x0, y0, x1, y1, WidgetRenderUtils.multiplyAlpha(color, this.alpha));
    }

    @ModifyExpressionValue(method = "renderButton", at = {@At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;textColor:I"), @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;textColorUneditable:I")})
    private int wrap_textColor_in_renderButton_JustZoom(int original) {
        return WidgetRenderUtils.multiplyAlpha(original, this.alpha);
    }

    /** @reason The old selection uses an unblendable color-logic operation. During a faded preview use an alpha-blended rectangle in the same bounds; retain the vanilla effect at full opacity. */
    @WrapMethod(method = "renderHighlight")
    private void wrap_renderHighlight_JustZoom(int x0, int y0, int x1, int y1, Operation<Void> original) {
        if (this.alpha >= 1.0F) {
            original.call(x0, y0, x1, y1);
        } else {
            GuiComponent.fill(new PoseStack(), Math.min(x0, this.x + this.width), y0, Math.min(x1, this.x + this.width), y1, WidgetRenderUtils.multiplyAlpha(0xFF0000FF, this.alpha));
        }
    }

}
