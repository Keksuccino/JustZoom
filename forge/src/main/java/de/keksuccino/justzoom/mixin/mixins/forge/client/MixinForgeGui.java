package de.keksuccino.justzoom.mixin.mixins.forge.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ForgeGui.class)
public class MixinForgeGui {

    /** @reason Forge replaces the vanilla HUD scope check; preserve keybind overlay support on its renderer. */
    @WrapOperation(method = "renderSpyglassOverlay", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z", remap = true))
    private boolean wrap_isScoping_JustZoom(LocalPlayer player, Operation<Boolean> original) {
        return original.call(player) || ZoomHandler.shouldShowSpyglassOverlay(false, ZoomHandler.isKeybindZooming());
    }

}
