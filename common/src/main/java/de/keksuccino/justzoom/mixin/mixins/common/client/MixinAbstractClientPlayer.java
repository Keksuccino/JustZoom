package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    /**
     * @reason Just Zoom modifies the finished camera FOV. Suppressing only the spyglass check here preserves vanilla's other FOV modifiers while preventing its hard-coded 0.1 factor from being applied first.
     */
    @WrapOperation(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isScoping()Z"))
    private boolean wrap_isScoping_in_getFieldOfViewModifier_JustZoom(AbstractClientPlayer instance, Operation<Boolean> original) {
        return !ZoomHandler.shouldUseJustZoomForSpyglass() && original.call(instance);
    }

}
