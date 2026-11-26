package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import de.keksuccino.justzoom.util.window.OffscreenTestClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Dev-only off-screen test client mode (see OffscreenTestClient and TESTING_GUIDELINES.md)
@Mixin(Window.class)
public class MixinWindow {

    /** @reason Creates the game window hidden and unfocused in the off-screen test client mode. */
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwDefaultWindowHints()V", remap = false))
    private void wrap_glfwDefaultWindowHints_JustZoom(Operation<Void> original) {
        original.call();
        OffscreenTestClient.prepareWindowCreation();
    }

    /** @reason Keeps off-screen test clients windowed, because GLFW always shows fullscreen windows. */
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/DisplayData;isFullscreen()Z"))
    private boolean modify_isFullscreen_JustZoom(boolean fullscreen) {
        return fullscreen && !OffscreenTestClient.isEnabled();
    }

    /** @reason Keeps off-screen test clients windowed, because GLFW always shows fullscreen windows. */
    @Inject(method = "toggleFullScreen", at = @At("HEAD"), cancellable = true)
    private void cancel_toggleFullScreen_JustZoom(CallbackInfo info) {
        if (OffscreenTestClient.isEnabled()) info.cancel();
    }

}
