package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public class MixinHud {

    @WrapOperation(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean wrap_isScoping_JustZoom(LocalPlayer instance, Operation<Boolean> original) {
        return original.call(instance) || ZoomHandler.shouldShowSpyglassOverlay(false, ZoomHandler.isKeybindZooming());
    }

    @WrapWithCondition(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSpyglassOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"))
    private boolean wrap_extractSpyglassOverlay_JustZoom(Hud instance, GuiGraphicsExtractor graphics, float scale) {
        return ZoomHandler.shouldShowSpyglassOverlay();
    }

}
