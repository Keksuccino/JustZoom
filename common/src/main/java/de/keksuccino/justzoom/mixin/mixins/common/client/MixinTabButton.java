package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.util.config.gui.WidgetRenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TabButton.class)
public abstract class MixinTabButton extends AbstractWidget {

    protected MixinTabButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    /** @reason Native tabs bypass widget alpha, which the world preview uses to fade controls. */
    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitNineSliced(Lnet/minecraft/resources/ResourceLocation;IIIIIIIIIIII)V"))
    private void wrap_blitNineSliced_in_renderWidget_JustZoom(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int left, int top, int right, int bottom, int textureWidth, int textureHeight, int u, int v, Operation<Void> original) {
        WidgetRenderUtils.withAlpha(graphics, this.alpha, () -> original.call(graphics, sprite, x, y, width, height, left, top, right, bottom, textureWidth, textureHeight, u, v));
    }

    /** @reason Tab labels need the same opacity as their textures. */
    @ModifyVariable(method = "renderString", at = @At("HEAD"), argsOnly = true)
    private int wrap_textColor_in_renderString_JustZoom(int color) {
        return WidgetRenderUtils.multiplyAlpha(color, this.alpha);
    }

    /** @reason Keep the selected-tab underline at the same preview opacity. */
    @WrapOperation(method = "renderFocusUnderline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void wrap_fill_in_renderFocusUnderline_JustZoom(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        original.call(graphics, x0, y0, x1, y1, WidgetRenderUtils.multiplyAlpha(color, this.alpha));
    }

}
