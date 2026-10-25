package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.util.SpyglassOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.justzoom.ZoomHandler;
import de.keksuccino.justzoom.ZoomMath;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "tickFov", at = @At("RETURN"))
    private void after_tickFov_JustZoom(CallbackInfo info) {
        ZoomHandler.onCameraTick();
    }

    /**
     * @reason Zooms the finished FOV after Vanilla, the loader and all other mods changed it. This wraps the whole method instead of injecting at RETURN:
     * once a RETURN callback cancels the method (like Simply Swords' getFov injection does), every RETURN callback applied after it is skipped, and the application order between mods is not stable.
     * The wrapper runs around all injected code, so zooming can neither be skipped by other mods nor skip their FOV changes. Do not turn this back into a RETURN injection.
     */
    @WrapMethod(method = "getFov")
    private double wrap_getFov_JustZoom(Camera camera, float partialTicks, boolean useFovSetting, Operation<Double> original) {
        double fov = original.call(camera, partialTicks, useFovSetting);
        // Vanilla also queries the hand projection with useFovSetting=false; only zoom the world projection.
        if (!useFovSetting) return fov;
        float normalFov = (float) fov;
        double magnification = ZoomHandler.getRenderedMagnification(partialTicks, normalFov);
        float modifiedFov = magnification > ZoomMath.MIN_MAGNIFICATION ? ZoomMath.calculateZoomedFov(normalFov, magnification) : normalFov;
        ZoomHandler.updateRenderedFov(normalFov, modifiedFov);
        return modifiedFov != normalFov ? modifiedFov : fov;
    }

    /** @reason Intercept the dispatch to Gui, since Forge replaces Gui.render and bypasses vanilla injections. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void wrap_renderHud_JustZoom(Gui gui, PoseStack graphics, float partialTicks, Operation<Void> original) {
        boolean hiddenByZoom = ZoomHandler.shouldHideHudWhileZooming();
        if (!hiddenByZoom) {
            original.call(gui, graphics, partialTicks);
        } else if (ZoomHandler.shouldExtractSpyglassOverlaySeparately(Minecraft.getInstance().options.hideGui, true, ZoomHandler.shouldShowSpyglassOverlay())) {
            ((SpyglassOverlayRenderer) gui).renderZoomOverlay_JustZoom(graphics);
        }
    }

}
