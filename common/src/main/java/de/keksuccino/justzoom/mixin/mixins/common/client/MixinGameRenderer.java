package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V"))
    private boolean wrap_renderItemInHand_JustZoom(GameRenderer instance, CameraRenderState cameraState, float deltaPartialTick, Matrix4fc modelViewMatrix) {
        return !ZoomHandler.shouldHideArmsWhenZooming();
    }

}
