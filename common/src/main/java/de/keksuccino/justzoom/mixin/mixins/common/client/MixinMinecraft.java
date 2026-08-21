package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.SpyglassSoundHandler;
import de.keksuccino.justzoom.ZoomHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "tick", at = @At("TAIL"))
    private void after_tick_JustZoom(CallbackInfo info) {
        SpyglassSoundHandler.onClientTick();
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void before_handleKeybinds_JustZoom(CallbackInfo info) {
        ZoomHandler.onInputTick();
    }

}
