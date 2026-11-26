package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import de.keksuccino.justzoom.util.window.OffscreenTestClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InputConstants.class)
public class MixinInputConstants {

    /**
     * @reason Off-screen test clients must never move or lock the mouse cursor of the user (see OffscreenTestClient).
     * SDL moves the real cursor even when the window is hidden, and the game treats its window as focused until SDL says otherwise.
     */
    @Inject(method = {"grabMouse", "releaseMouse"}, at = @At("HEAD"), cancellable = true)
    private static void cancel_grabOrReleaseMouse_JustZoom(Window window, double xpos, double ypos, CallbackInfo info) {
        if (OffscreenTestClient.isEnabled()) info.cancel();
    }

}
