package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.JustZoom;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void return_calculateFov_JustZoom(float partialTicks, CallbackInfoReturnable<Float> info) {

        if (ZoomHandler.isZooming() && !ZoomHandler.shouldZoomInOutSmooth()) {
            float normalFov = info.getReturnValue();
            if (normalFov > 170.0F) normalFov = 170.0F;
            if (normalFov < 1.0F) normalFov = 1.0F;

            float modifiedFov = normalFov * ZoomHandler.getFovModifier();
            if (modifiedFov > 170.0F) modifiedFov = 170.0F;
            if (modifiedFov < 1.0F) modifiedFov = 1.0F;

            ZoomHandler.cachedNormalFov = normalFov;
            ZoomHandler.cachedModifiedFov = modifiedFov;
            info.setReturnValue(modifiedFov);
        } else if (JustZoom.getOptions().resetZoomFactorOnStopZooming.getValue()) {
            ZoomHandler.zoomModifier = JustZoom.getOptions().baseZoomFactor.getValue();
        }

    }

}
