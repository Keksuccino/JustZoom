package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.keksuccino.justzoom.JustZoom;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    /**
     * @reason This is a basic "Mouse Scroll Event" implementation for Just Zoom. It is cancelable to stop the hotbar slot from changing while using the mouse wheel to adjust the zoom factor.
     */
    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
    private void before_isSpectator_in_onScroll_JustZoom(long $$0, double $$1, double $$2, CallbackInfo info) {

        boolean discreteScroll = Minecraft.getInstance().options.discreteMouseScroll;
        double sensitivity = Minecraft.getInstance().options.mouseWheelSensitivity;
        double deltaX = (discreteScroll ? Math.signum($$1) : $$1) * sensitivity;
        double deltaY = (discreteScroll ? Math.signum($$2) : $$2) * sensitivity;

        ZoomHandler.MouseScrollFeedback feedback = new ZoomHandler.MouseScrollFeedback();
        ZoomHandler.onMouseScroll(feedback, deltaX, deltaY);
        if (feedback.cancel) info.cancel();

    }

    /**
     * @reason This is to normalize the mouse sensitivity when zooming with Just Zoom (if the option for that is enabled).
     */
    @WrapOperation(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;sensitivity:D"))
    private double wrap_sensitivity_in_turnPlayer_JustZoom(Options instance, Operation<Double> original) {
        if (ZoomHandler.isZooming() && JustZoom.getOptions().normalizeMouseSensitivityOnZoom.getValue()) {
            double sensitivity = original.call(instance);
            // Calculate zoom ratio (smaller = more zoomed in)
            double zoomRatio = ZoomHandler.getFovModifier(); // This is the actual zoom factor

            // Use a direct linear relationship with the zoom factor
            // This is the simplest and most predictable approach
            double scale = zoomRatio;

            // Apply additional quadratic scaling for extreme zoom levels
            // This makes the sensitivity reduction much more aggressive as zoom increases
            if (zoomRatio < 0.5) {
                // Square the zoom ratio for a stronger effect at high zoom levels
                scale = zoomRatio * zoomRatio;
            }

            // For very extreme zoom (near maximum zoom), apply even more aggressive reduction
            if (zoomRatio < 0.1) {
                // Apply cubic scaling for extreme zoom
                scale = Math.pow(zoomRatio, 3);
            }

            return sensitivity * scale; // Dramatically reduced sensitivity at high zoom
        }
        return original.call(instance);
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