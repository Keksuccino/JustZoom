package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.Window;
import de.keksuccino.justzoom.util.window.OffscreenTestClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Dev-only off-screen test client mode (see OffscreenTestClient and TESTING_GUIDELINES.md)
@Mixin(Window.class)
public class MixinWindow {

    /** @reason Creates the game window hidden in the off-screen test client mode. */
    @ModifyArg(method = "createWindow", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuBackend;createWindow(Ljava/lang/String;IIJ)J"), index = 3)
    private long modify_createWindow_JustZoom(long flags) {
        return OffscreenTestClient.modifyWindowFlags(flags);
    }

    /** @reason Keeps off-screen test clients windowed, because SDL shows fullscreen windows. */
    // Targets all constructors, because the constructor without maximum window size only delegates to the other one
    @ModifyExpressionValue(method = "<init>*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/DisplayData;isFullscreen()Z"))
    private boolean modify_isFullscreen_JustZoom(boolean fullscreen) {
        return fullscreen && !OffscreenTestClient.isEnabled();
    }

    /** @reason Keeps off-screen test clients windowed, because SDL shows fullscreen windows. */
    @Inject(method = "setFullscreen", at = @At("HEAD"), cancellable = true)
    private void cancel_setFullscreen_JustZoom(boolean fullscreen, CallbackInfo info) {
        if (fullscreen && OffscreenTestClient.isEnabled()) info.cancel();
    }

}
