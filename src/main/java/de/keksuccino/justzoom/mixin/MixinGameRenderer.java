package de.keksuccino.justzoom.mixin;

import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(at = @At("RETURN"), method = "getFov", cancellable = true)
    public void onGetFOVModifier(Camera c, float partial, boolean useFOVSetting, CallbackInfoReturnable<Double> info) {

        double defaultFOV = info.getReturnValue();
        info.setReturnValue(ZoomHandler.handleZoom(defaultFOV));

    }

}
