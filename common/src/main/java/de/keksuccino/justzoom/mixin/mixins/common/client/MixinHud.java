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
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public abstract class MixinHud {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private boolean isHidden;
    @Shadow private float scopeScale;

    @Unique private boolean showSpyglassOverlay_JustZoom;
    @Unique private boolean useJustZoomSpyglassOverlayAnimation_JustZoom;

    @Shadow protected abstract void extractSpyglassOverlay(GuiGraphicsExtractor graphics, float scale);

    /**
     * @reason Reusing the HUD's own visibility gates works for both Fabric's direct renderer and NeoForge's layered renderer. The spyglass overlay is extracted separately because it has its own visibility setting, while the real hidden state and render flag must be restored so this setting does not also hide hands like vanilla's HUD toggle.
     */
    @WrapMethod(method = "extractRenderState")
    private void wrap_extractRenderState_JustZoom(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        boolean originallyHidden = this.isHidden;
        boolean hiddenByZoom = ZoomHandler.shouldHideHudWhileZooming();
        this.showSpyglassOverlay_JustZoom = ZoomHandler.shouldShowSpyglassOverlay() && this.minecraft.options.getCameraType().isFirstPerson();
        this.useJustZoomSpyglassOverlayAnimation_JustZoom = ZoomHandler.isZooming();
        if (ZoomHandler.shouldExtractSpyglassOverlaySeparately(originallyHidden, hiddenByZoom, this.showSpyglassOverlay_JustZoom)) {
            if (!this.useJustZoomSpyglassOverlayAnimation_JustZoom) {
                // The hidden-HUD path bypasses vanilla's camera-overlay layer, so advance its own field with vanilla's exact formula before drawing separately.
                this.scopeScale = Mth.lerp(0.5F * deltaTracker.getGameTimeDeltaTicks(), this.scopeScale, 1.125F);
            }
            this.extractSpyglassOverlay(graphics, ZoomHandler.getSpyglassOverlayScale(this.useJustZoomSpyglassOverlayAnimation_JustZoom, this.scopeScale));
        }
        this.isHidden = originallyHidden || hiddenByZoom;
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
    private boolean cancel_extractSpyglassOverlay_JustZoom(Hud instance, GuiGraphicsExtractor graphics, float scale) {
        if (this.showSpyglassOverlay_JustZoom) {
            // Keep the original extraction position when the HUD is visible, but own the call so the hidden-HUD path can use the same overlay state.
            this.extractSpyglassOverlay(graphics, ZoomHandler.getSpyglassOverlayScale(this.useJustZoomSpyglassOverlayAnimation_JustZoom, scale));
        }
        return false;
    }

}
