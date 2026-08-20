package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.JustZoom;
import de.keksuccino.justzoom.ZoomHandler;
import de.keksuccino.justzoom.ZoomMath;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {

    @Shadow private float fovModifier;
    @Shadow private float oldFovModifier;

    /**
     * @reason Minecraft's vanilla FOV interpolation clamps modifiers to the spyglass limit of 0.1. Just Zoom keeps that interpolation but needs its wider modifier range to reach the same 1-degree maximum zoom as the non-smooth path.
     */
    @WrapOperation(method = "tickFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float wrap_clamp_in_tickFov_JustZoom(float value, float minimum, float maximum, Operation<Float> original) {
        boolean expandedZoomRange = ZoomHandler.isZooming() && ZoomHandler.shouldZoomInOutSmooth();
        return original.call(value, ZoomMath.selectFovModifierMinimum(expandedZoomRange, minimum), maximum);
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void return_calculateFov_JustZoom(float partialTicks, CallbackInfoReturnable<Float> info) {

        boolean zooming = ZoomHandler.isZooming();
        if (zooming && !ZoomHandler.shouldZoomInOutSmooth()) {
            float normalFov = ZoomMath.clampFov(info.getReturnValue());
            float modifiedFov = ZoomMath.calculateZoomedFov(normalFov, ZoomHandler.getFovModifier());
            ZoomHandler.cachedNormalFov = normalFov;
            ZoomHandler.cachedModifiedFov = modifiedFov;
            info.setReturnValue(modifiedFov);
        } else if (ZoomMath.shouldClampFinalFov(zooming, this.oldFovModifier, this.fovModifier)) {
            // Keep the projection safe until vanilla's interpolated modifier has fully returned above its normal minimum after zooming stops.
            info.setReturnValue(ZoomMath.clampFov(info.getReturnValue()));
        }

        if (!zooming && JustZoom.getOptions().resetZoomFactorOnStopZooming.getValue()) {
            ZoomHandler.zoomModifier = JustZoom.getOptions().baseZoomFactor.getValue();
        }

    }

}
