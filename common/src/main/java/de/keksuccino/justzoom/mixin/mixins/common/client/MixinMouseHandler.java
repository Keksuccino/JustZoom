package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
    private void before_isSpectator_in_onScroll_JustZoom(long $$0, double $$1, double $$2, CallbackInfo info) {

        boolean discreteScroll = Minecraft.getInstance().options.discreteMouseScroll().get();
        double sensitivity = Minecraft.getInstance().options.mouseWheelSensitivity().get();
        double deltaX = (discreteScroll ? Math.signum($$1) : $$1) * sensitivity;
        double deltaY = (discreteScroll ? Math.signum($$2) : $$2) * sensitivity;

        ZoomHandler.MouseScrollFeedback feedback = new ZoomHandler.MouseScrollFeedback();
        ZoomHandler.onMouseScroll(feedback, deltaX, deltaY);
        if (feedback.cancel) info.cancel();

    }

    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean wrap_isFirstPerson_in_turnPlayer_JustZoom(CameraType instance, Operation<Boolean> original) {
        if (ZoomHandler.isZooming()) return true;
        else return original.call(instance);
    }

    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean wrap_isScoping_in_turnPlayer_JustZoom(LocalPlayer instance, Operation<Boolean> original) {
        if (ZoomHandler.isZooming()) return true;
        else return original.call(instance);
    }

}
