package de.keksuccino.justzoom.mixin.mixins.common.client;

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
    private void after_getFov_JustZoom(Camera camera, float partialTicks, boolean useFovSetting, CallbackInfoReturnable<Float> info) {
        // Vanilla also queries the hand projection with useFovSetting=false; only zoom the world projection.
        if (!useFovSetting) return;
        float normalFov = info.getReturnValue();
        double magnification = ZoomHandler.getRenderedMagnification(partialTicks, normalFov);
        float modifiedFov = magnification > ZoomMath.MIN_MAGNIFICATION ? ZoomMath.calculateZoomedFov(normalFov, magnification) : normalFov;
        ZoomHandler.updateRenderedFov(normalFov, modifiedFov);
        if (modifiedFov != normalFov) {
            info.setReturnValue(modifiedFov);
        }
    }

}
