package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private float scopeScale;

    @Unique private boolean showSpyglassOverlay_JustZoom;
    @Unique private boolean useJustZoomSpyglassOverlayAnimation_JustZoom;

    @Shadow
    protected abstract void renderSpyglassOverlay(GuiGraphics graphics, float scale);

    /**
     * @reason Reusing the HUD's own visibility gates works for both Fabric's direct renderer and NeoForge's layered renderer. The spyglass overlay is extracted separately because it has its own visibility setting, while the real hidden state and render flag must be restored so this setting does not also hide hands like vanilla's HUD toggle.
     */
    @WrapMethod(method = "render")
    private void wrap_render_JustZoom(GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        boolean originallyHidden = this.minecraft.options.hideGui;
        boolean hiddenByZoom = ZoomHandler.shouldHideHudWhileZooming();
        this.showSpyglassOverlay_JustZoom = ZoomHandler.shouldShowSpyglassOverlay() && this.minecraft.options.getCameraType().isFirstPerson();
        this.useJustZoomSpyglassOverlayAnimation_JustZoom = ZoomHandler.isZooming();
        if (ZoomHandler.shouldExtractSpyglassOverlaySeparately(originallyHidden, hiddenByZoom, this.showSpyglassOverlay_JustZoom)) {
            if (!this.useJustZoomSpyglassOverlayAnimation_JustZoom) {
                // The hidden-HUD path bypasses vanilla's camera-overlay layer, so advance its own field with vanilla's exact formula before drawing separately.
                this.scopeScale = Mth.lerp(0.5F * deltaTracker.getGameTimeDeltaTicks(), this.scopeScale, 1.125F);
            }
            this.renderSpyglassOverlay(graphics, ZoomHandler.getSpyglassOverlayScale(this.useJustZoomSpyglassOverlayAnimation_JustZoom, this.scopeScale));
        }
        this.minecraft.options.hideGui = originallyHidden || hiddenByZoom;
        try {
            original.call(graphics, deltaTracker);
        } finally {
            this.minecraft.options.hideGui = originallyHidden;
        }
    }

    @WrapOperation(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean wrap_isScoping_JustZoom(LocalPlayer instance, Operation<Boolean> original) {
        return original.call(instance) || ZoomHandler.shouldShowSpyglassOverlay(false, ZoomHandler.isKeybindZooming());
    }

    @WrapWithCondition(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    private boolean cancel_renderSpyglassOverlay_JustZoom(Gui instance, GuiGraphics graphics, float scale) {
        if (this.showSpyglassOverlay_JustZoom) {
            // Keep the original extraction position when the HUD is visible, but own the call so the hidden-HUD path can use the same overlay state.
            this.renderSpyglassOverlay(graphics, ZoomHandler.getSpyglassOverlayScale(this.useJustZoomSpyglassOverlayAnimation_JustZoom, scale));
        }
        return false;
    }

}
