package de.keksuccino.justzoom.mixin;

import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(at = @At("RETURN"), method = "getFOVModifier", cancellable = true)
    public void onGetFOVModifier(CallbackInfoReturnable<Double> info) {

        double defaultFOV = info.getReturnValue();
        info.setReturnValue(ZoomHandler.handleZoom(defaultFOV));

    }

}
