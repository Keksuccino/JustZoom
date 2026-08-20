package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ZoomHandler;
import de.keksuccino.justzoom.ZoomMath;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {

    @Inject(method = "tickFov", at = @At("RETURN"))
    private void after_tickFov_JustZoom(CallbackInfo info) {
        ZoomHandler.onCameraTick();
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void return_calculateFov_JustZoom(float partialTicks, CallbackInfoReturnable<Float> info) {
        float normalFov = info.getReturnValue();
        double magnification = ZoomHandler.getRenderedMagnification(partialTicks, normalFov);
        float modifiedFov = magnification > ZoomMath.MIN_MAGNIFICATION ? ZoomMath.calculateZoomedFov(normalFov, magnification) : normalFov;
        ZoomHandler.updateRenderedFov(normalFov, modifiedFov);
        if (modifiedFov != normalFov) {
            info.setReturnValue(modifiedFov);
        }
    }

}
