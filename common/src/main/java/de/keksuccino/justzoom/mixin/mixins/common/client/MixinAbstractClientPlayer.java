package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void head_getFieldOfViewModifier_JustZoom(CallbackInfoReturnable<Float> info) {
        if (ZoomHandler.isZooming() && ZoomHandler.shouldZoomInOutSmooth()) {
            info.setReturnValue(ZoomHandler.getFovModifier());
        }
    }

}
