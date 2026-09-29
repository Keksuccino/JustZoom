package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.util.config.gui.WidgetRenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TabNavigationBar.class)
public abstract class MixinTabNavigationBar {

    @Shadow @Final private ImmutableList<TabButton> tabButtons;

    /** @reason Tab separators are drawn outside the buttons, so inherit the same preview opacity from the first tab. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"))
    private void wrap_blit_in_render_JustZoom(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        float alpha = ((AccessorMixinAbstractWidget) this.tabButtons.get(0)).get_alpha_JustZoom();
        WidgetRenderUtils.withAlpha(graphics, alpha, () -> original.call(graphics, texture, x, y, u, v, width, height, textureWidth, textureHeight));
    }

    /** @reason The native 1.20 tab bar's solid header also follows preview opacity. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"))
    private void wrap_fill_in_render_JustZoom(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        float alpha = ((AccessorMixinAbstractWidget) this.tabButtons.get(0)).get_alpha_JustZoom();
        original.call(graphics, x0, y0, x1, y1, WidgetRenderUtils.multiplyAlpha(color, alpha));
    }

}
