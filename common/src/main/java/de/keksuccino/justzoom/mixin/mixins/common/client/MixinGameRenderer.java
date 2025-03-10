package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.keksuccino.justzoom.JustZoom;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Shadow private float fovModifier;
    @Shadow private float oldFovModifier;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void return_getFov_JustZoom(Camera c, float partial, boolean useFOVSetting, CallbackInfoReturnable<Float> info) {

        if (ZoomHandler.isZooming()) {
            float normalFov = info.getReturnValue();
            if (normalFov > 170.0F) normalFov = 170.0F;
            if (normalFov < 1.0F) normalFov = 1.0F;
            float modifiedFov = normalFov;
            ZoomHandler.cachedNormalFov = normalFov;
            modifiedFov = modifiedFov * ZoomHandler.getFovModifier();
            if (modifiedFov > 170.0F) modifiedFov = 170.0F;
            if (modifiedFov < 1.0F) modifiedFov = 1.0F;
            ZoomHandler.cachedModifiedFov = modifiedFov;
            if (!ZoomHandler.shouldZoomInOutSmooth()) {
                info.setReturnValue(modifiedFov);
            } else {
                info.setReturnValue(normalFov);
            }
        } else if (JustZoom.getOptions().resetZoomFactorOnStopZooming.getValue()) {
            ZoomHandler.zoomModifier = JustZoom.getOptions().baseZoomFactor.getValue();
        }

    }

    @Inject(method = "tickFov", at = @At("HEAD"), cancellable = true)
    private void head_tickFov_JustZoom(CallbackInfo info) {

        if (ZoomHandler.isZooming()) {

            info.cancel();

            float f = 1.0F;
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity instanceof AbstractClientPlayer player) {
                Options options = Minecraft.getInstance().options;
                boolean firstPerson = options.getCameraType().isFirstPerson();
                float fovEffectScale = options.fovEffectScale().get().floatValue();
                f = player.getFieldOfViewModifier(firstPerson, fovEffectScale);
            }

            this.oldFovModifier = this.fovModifier;
            this.fovModifier += (f - this.fovModifier) * 0.5F;

        }

    }

    @WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/Camera;FLorg/joml/Matrix4f;)V"))
    private boolean wrap_renderItemInHand_JustZoom(GameRenderer instance, Camera camera, float f, Matrix4f matrix4f) {
        return !ZoomHandler.shouldHideArmsWhenZooming();
    }

}