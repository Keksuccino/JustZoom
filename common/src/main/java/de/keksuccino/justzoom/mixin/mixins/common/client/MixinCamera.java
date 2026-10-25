package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import de.keksuccino.justzoom.ZoomHandler;
import de.keksuccino.justzoom.ZoomMath;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class MixinCamera {

    @Inject(method = "tickFov", at = @At("RETURN"))
    private void after_tickFov_JustZoom(CallbackInfo info) {
        ZoomHandler.onCameraTick();
    }

    /**
     * @reason Zooms the finished FOV after Vanilla, the loader and all other mods changed it. This wraps the whole method instead of injecting at RETURN:
     * once a RETURN callback cancels the method (like Simply Swords' getFov injection does on older Minecraft versions), every RETURN callback applied after it is skipped, and the application order between mods is not stable.
     * The wrapper runs around all injected code, so zooming can neither be skipped by other mods nor skip their FOV changes. Do not turn this back into a RETURN injection.
     */
    @WrapMethod(method = "calculateFov")
    private float wrap_calculateFov_JustZoom(float partialTicks, Operation<Float> original) {
        float normalFov = original.call(partialTicks);
        double magnification = ZoomHandler.getRenderedMagnification(partialTicks, normalFov);
        float modifiedFov = magnification > ZoomMath.MIN_MAGNIFICATION ? ZoomMath.calculateZoomedFov(normalFov, magnification) : normalFov;
        ZoomHandler.updateRenderedFov(normalFov, modifiedFov);
        return modifiedFov;
    }

}
