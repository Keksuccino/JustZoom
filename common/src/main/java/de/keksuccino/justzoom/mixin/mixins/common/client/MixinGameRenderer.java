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
            double normalFov = info.getReturnValue();
            if (normalFov > 170.0D) normalFov = 170.0D;
            if (normalFov < 1.0D) normalFov = 1.0D;
            double modifiedFov = normalFov;
            ZoomHandler.cachedNormalFov = normalFov;
            modifiedFov = modifiedFov * ZoomHandler.getFovModifier();
            if (modifiedFov > 170.0D) modifiedFov = 170.0D;
            if (modifiedFov < 1.0D) modifiedFov = 1.0D;
            ZoomHandler.cachedModifiedFov = modifiedFov;
            if (!ZoomHandler.shouldZoomInOutSmooth()) {
                info.setReturnValue(modifiedFov);
            } else {
                info.setReturnValue(normalFov);
            }
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
