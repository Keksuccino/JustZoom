package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.JustZoom;
import de.keksuccino.justzoom.KeyMappings;
import de.keksuccino.justzoom.ZoomHandler;
import de.keksuccino.justzoom.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    /**
     * @reason Vanilla keybind screens do not model wheel directions. Capture wheel input only while one of Just Zoom's wheel-capable mappings is selected, then preserve Vanilla's mapping refresh flow.
     */
    @WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"))
    private boolean wrap_mouseScrolled_in_onScroll_JustZoom(Screen instance, double mouseX, double mouseY, double deltaX, double deltaY, Operation<Boolean> original) {
        if (instance instanceof KeyBindsScreen keyBindsScreen && keyBindsScreen.selectedKey != null && KeyMappings.isZoomAdjustment(keyBindsScreen.selectedKey) && KeyMappings.hasMouseWheelDirection(deltaY)) {
            Services.PLATFORM.setKeyMappingKey(keyBindsScreen.selectedKey, KeyMappings.getMouseWheelKey(deltaY));
            keyBindsScreen.selectedKey = null;
            ((AccessorMixinKeyBindsScreen) keyBindsScreen).get_keyBindsList_JustZoom().resetMappingAndUpdateButtons();
            return true;
        }
        return original.call(instance, mouseX, mouseY, deltaX, deltaY);
    }

    /**
     * @reason Vanilla applies its own spyglass sensitivity reduction. When the spyglass uses Just Zoom, skipping that branch makes sensitivity follow the same configurable normalization path as the zoom keybind.
     */
    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean wrap_isScoping_in_turnPlayer_JustZoom(LocalPlayer instance, Operation<Boolean> original) {
        return !ZoomHandler.shouldUseJustZoomForSpyglass() && original.call(instance);
    }

    /**
     * @reason This is a basic "Mouse Scroll Event" implementation for Just Zoom. It is cancelable to stop the hotbar slot from changing while using the mouse wheel to adjust the zoom factor.
     */
    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
    private void before_isSpectator_in_onScroll_JustZoom(long $$0, double $$1, double $$2, CallbackInfo info) {

        boolean discreteScroll = Minecraft.getInstance().options.discreteMouseScroll().get();
        double sensitivity = Minecraft.getInstance().options.mouseWheelSensitivity().get();
        double deltaY = (discreteScroll ? Math.signum($$2) : $$2) * sensitivity;

        ZoomHandler.MouseScrollFeedback feedback = new ZoomHandler.MouseScrollFeedback();
        ZoomHandler.onMouseScroll(feedback, deltaY);
        if (feedback.cancel) info.cancel();

    }

    /**
     * @reason Scaling the completed vanilla turn delta avoids the non-zero floor in Minecraft's sensitivity formula and keeps on-screen camera motion proportional at every magnification.
     */
    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void wrap_turn_in_turnPlayer_JustZoom(LocalPlayer instance, double deltaX, double deltaY, Operation<Void> original) {
        double scale = JustZoom.getOptions().normalizeMouseSensitivityOnZoom.getValue() ? ZoomHandler.getMouseSensitivityScale() : 1.0D;
        original.call(instance, deltaX * scale, deltaY * scale);
    }

    /**
     * @reason Forces the camera to be smooth when zooming with Just Zoom (if the option for that is enabled).
     */
    @WrapOperation(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
    private boolean wrap_smoothCamera_in_turnPlayer_JustZoom(Options instance, Operation<Boolean> original) {
        if (ZoomHandler.isZooming() && JustZoom.getOptions().smoothCameraOnZoom.getValue()) {
            return true;
        }
        return original.call(instance);
    }

}
