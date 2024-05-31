package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class MixinCameraType {

    @Inject(method = "isFirstPerson", at = @At("HEAD"), cancellable = true)
    private void head_isFirstPerson_JustZoom(CallbackInfoReturnable<Boolean> info) {
        if (ZoomHandler.isZooming()) info.setReturnValue(true);
    }

}
