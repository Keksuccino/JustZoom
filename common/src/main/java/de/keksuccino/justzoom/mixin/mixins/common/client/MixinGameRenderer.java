package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Shadow private float fov;
    @Shadow private float oldFov;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void return_getFov_JustZoom(Camera c, float partial, boolean useFOVSetting, CallbackInfoReturnable<Double> info) {

        if (ZoomHandler.isZooming()) {
            double finalFov = info.getReturnValue();
            if (!ZoomHandler.shouldZoomInOutSmooth()) {
                finalFov = finalFov * ZoomHandler.getFovModifier();
            }
            if (finalFov > 170.0D) finalFov = 170.0D;
            if (finalFov < 1.0D) finalFov = 1.0D;
            info.setReturnValue(finalFov);
        }

    }

    @Inject(method = "tickFov", at = @At("HEAD"), cancellable = true)
    private void head_tickFov_JustZoom(CallbackInfo info) {

        if (ZoomHandler.isZooming()) {

            info.cancel();

            float f = 1.0F;
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity instanceof AbstractClientPlayer abstractclientplayer) {
                f = abstractclientplayer.getFieldOfViewModifier();
            }

            this.oldFov = this.fov;
            this.fov += (f - this.fov) * 0.5F;

        }

    }

}
