package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.ZoomHandler;
import de.keksuccino.justzoom.util.SpyglassOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class MixinGui implements SpyglassOverlayRenderer {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private int screenWidth;
    @Shadow private int screenHeight;
    @Shadow private float scopeScale;

    @Shadow
    protected abstract void renderSpyglassOverlay(GuiGraphics graphics, float scale);

    @Override
    public void renderZoomOverlay_JustZoom(GuiGraphics graphics) {
        // Gui.render normally initializes these fields. Forge overrides that method, so initialize
        // them here as well when skipping the HUD; otherwise resize/fullscreen leaves a stale scope.
        this.screenWidth = graphics.guiWidth();
        this.screenHeight = graphics.guiHeight();
        this.scopeScale = Mth.lerp(0.5F * this.minecraft.getDeltaFrameTime(), this.scopeScale, 1.125F);
        this.renderSpyglassOverlay(graphics, this.scopeScale);
    }

    /** @reason Keybind zoom can opt into the same overlay as the physical spyglass. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean wrap_isScoping_JustZoom(LocalPlayer instance, Operation<Boolean> original) {
        return original.call(instance) || ZoomHandler.shouldShowSpyglassOverlay(false, ZoomHandler.isKeybindZooming());
    }

    /** @reason Both vanilla and Forge ultimately call this method, sharing overlay visibility and animation. */
    @WrapMethod(method = "renderSpyglassOverlay")
    private void wrap_renderSpyglassOverlay_JustZoom(GuiGraphics graphics, float scale, Operation<Void> original) {
        if (ZoomHandler.shouldShowSpyglassOverlay() && this.minecraft.options.getCameraType().isFirstPerson()) {
            original.call(graphics, ZoomHandler.getSpyglassOverlayScale(ZoomHandler.isZooming(), scale));
        }
    }

}
