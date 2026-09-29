package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.util.SpyglassOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.justzoom.ZoomHandler;
import de.keksuccino.justzoom.ZoomMath;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "tickFov", at = @At("RETURN"))
    private void after_tickFov_JustZoom(CallbackInfo info) {
        ZoomHandler.onCameraTick();
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void after_getFov_JustZoom(Camera camera, float partialTicks, boolean useFovSetting, CallbackInfoReturnable<Double> info) {
        // Vanilla also queries the hand projection with useFovSetting=false; only zoom the world projection.
        if (!useFovSetting) return;
        float normalFov = info.getReturnValue().floatValue();
        double magnification = ZoomHandler.getRenderedMagnification(partialTicks, normalFov);
        float modifiedFov = magnification > ZoomMath.MIN_MAGNIFICATION ? ZoomMath.calculateZoomedFov(normalFov, magnification) : normalFov;
        ZoomHandler.updateRenderedFov(normalFov, modifiedFov);
        if (modifiedFov != normalFov) {
            info.setReturnValue((double) modifiedFov);
        }
    }

    /** @reason Intercept the dispatch to Gui, since Forge replaces Gui.render and bypasses vanilla injections. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    private void wrap_renderHud_JustZoom(Gui gui, GuiGraphics graphics, float partialTicks, Operation<Void> original) {
        boolean hiddenByZoom = ZoomHandler.shouldHideHudWhileZooming();
        if (!hiddenByZoom) {
            original.call(gui, graphics, partialTicks);
        } else if (ZoomHandler.shouldExtractSpyglassOverlaySeparately(Minecraft.getInstance().options.hideGui, true, ZoomHandler.shouldShowSpyglassOverlay())) {
            ((SpyglassOverlayRenderer) gui).renderZoomOverlay_JustZoom(graphics);
        }
    }

}
