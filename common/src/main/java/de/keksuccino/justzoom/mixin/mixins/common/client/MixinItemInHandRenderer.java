package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    /**
     * @reason Iris renders shader hand passes through ItemInHandRenderer directly, bypassing GameRenderer.renderLevel. Cancelling each hand here catches both vanilla and Iris while allowing the active renderer to finish its buffer and projection cleanup.
     */
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void before_submitArmWithItem_JustZoom(CallbackInfo info) {
        if (ZoomHandler.shouldHideArmsWhenZooming()) {
            info.cancel();
        }
    }

}
