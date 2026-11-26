package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.keksuccino.justzoom.OpenOptionsToastHandler;
import de.keksuccino.justzoom.OptionsScreen;
import de.keksuccino.justzoom.SpyglassSoundHandler;
import de.keksuccino.justzoom.ZoomHandler;
import de.keksuccino.justzoom.util.window.OffscreenTestClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    /** @reason Keeps the game window hidden in the dev-only off-screen test client mode (see OffscreenTestClient and TESTING_GUIDELINES.md). */
    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwShowWindow(J)V", remap = false))
    private boolean cancel_glfwShowWindow_JustZoom(long window) {
        return !OffscreenTestClient.isEnabled();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void after_tick_JustZoom(CallbackInfo info) {
        SpyglassSoundHandler.onClientTick();
        OpenOptionsToastHandler.onClientTick();
        OffscreenTestClient.onClientTick((Minecraft)(Object)this);
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void before_handleKeybinds_JustZoom(CallbackInfo info) {
        ZoomHandler.onInputTick();
    }

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void after_handleKeybinds_JustZoom(CallbackInfo info) {
        OptionsScreen.openFromKeybind();
    }

}
