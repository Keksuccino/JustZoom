package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.JustZoom;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {

    @Unique
    private static final float MIN_FOV_JUSTZOOM = 1.0F;

    /** @reason Let Just Zoom's smooth FOV interpolation reach below the vanilla spyglass limit without changing vanilla's upper limit. */
    @WrapOperation(method = "tickFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float wrap_clamp_in_tickFov_JustZoom(float value, float min, float max, Operation<Float> original) {
        if (ZoomHandler.isZooming() && ZoomHandler.shouldZoomInOutSmooth()) {
            // Use a fixed positive floor: clamping to the requested zoom factor would
            // skip interpolation when scrolling back out from a stronger zoom.
            min = Math.min(min, ZoomHandler.MIN_FOV_MODIFIER);
        }
        return original.call(value, min, max);
    }

    /** @reason Apply instant zoom and enforce the same minimum visible FOV for both zoom modes. */
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void return_calculateFov_JustZoom(float partialTicks, CallbackInfoReturnable<Float> info) {

        boolean zooming = ZoomHandler.isZooming();
        if (zooming && !ZoomHandler.shouldZoomInOutSmooth()) {
            float normalFov = info.getReturnValue();
            if (normalFov > 170.0F) normalFov = 170.0F;
            if (normalFov < MIN_FOV_JUSTZOOM) normalFov = MIN_FOV_JUSTZOOM;

            float modifiedFov = normalFov * ZoomHandler.getFovModifier();
            if (modifiedFov > 170.0F) modifiedFov = 170.0F;
            if (modifiedFov < MIN_FOV_JUSTZOOM) modifiedFov = MIN_FOV_JUSTZOOM;

            ZoomHandler.cachedNormalFov = normalFov;
            ZoomHandler.cachedModifiedFov = modifiedFov;
            info.setReturnValue(modifiedFov);
        } else if (zooming) {
            // Bound the rendered FOV after interpolation and fluid/death effects so a
            // near-zero smooth zoom target cannot keep visibly magnifying the scene.
            if (info.getReturnValue() < MIN_FOV_JUSTZOOM) info.setReturnValue(MIN_FOV_JUSTZOOM);
        } else if (JustZoom.getOptions().resetZoomFactorOnStopZooming.getValue()) {
            ZoomHandler.zoomModifier = JustZoom.getOptions().baseZoomFactor.getValue();
        }

    }

}
