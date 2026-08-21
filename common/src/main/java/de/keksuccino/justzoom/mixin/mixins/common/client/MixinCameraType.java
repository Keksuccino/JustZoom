package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class MixinCameraType {

    /** @reason Make rear third-person zoom use the unobstructed first-person camera when the improvement is enabled. */
    @Inject(method = "isFirstPerson", at = @At("HEAD"), cancellable = true)
    private void head_isFirstPerson_JustZoom(CallbackInfoReturnable<Boolean> info) {
        if (ZoomHandler.shouldUseFirstPersonCameraWhileZooming()) {
            info.setReturnValue(true);
        }
    }

}
