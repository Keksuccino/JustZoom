package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public class MixinHud {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private boolean isHidden;

    /**
     * @reason Reusing the HUD's own visibility gates works for both Fabric's direct renderer and NeoForge's layered renderer. The real hidden state and render flag must be restored so this setting does not also hide hands and first-person screen effects like vanilla's HUD toggle.
     */
    @WrapMethod(method = "extractRenderState")
    private void wrap_extractRenderState_JustZoom(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        boolean originallyHidden = this.isHidden;
        this.isHidden = originallyHidden || ZoomHandler.shouldHideHudWhileZooming();
        try {
            original.call(graphics, deltaTracker);
        } finally {
            this.isHidden = originallyHidden;
            this.minecraft.gameRenderer.gameRenderState().guiRenderState.isHudHidden = originallyHidden;
        }
    }

    @WrapOperation(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean wrap_isScoping_JustZoom(LocalPlayer instance, Operation<Boolean> original) {
        return original.call(instance) || ZoomHandler.shouldShowSpyglassOverlay(false, ZoomHandler.isKeybindZooming());
    }

    @WrapWithCondition(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSpyglassOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"))
    private boolean wrap_extractSpyglassOverlay_JustZoom(Hud instance, GuiGraphicsExtractor graphics, float scale) {
        return ZoomHandler.shouldShowSpyglassOverlay();
    }

}
