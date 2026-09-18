package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class MixinFirstPersonHandsAndItemsRenderer {

    /**
     * @reason Hide hands at the shared submission point: 26.3's 3D HUD and Iris's
     * separate shader passes both call this method. Cancel before any poses or
     * geometry are submitted, leaving each caller's render cleanup and screen effects intact.
     */
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void cancel_submitHandsWithItems_JustZoom(CallbackInfo info) {
        if (ZoomHandler.shouldHideArmsWhenZooming()) {
            info.cancel();
        }
    }

}
