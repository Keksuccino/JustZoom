package de.keksuccino.justzoom.mixin.mixins.neoforge.client;

import de.keksuccino.justzoom.JustZoom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void after_construct_JustZoom(GameConfig gameConfig, CallbackInfo info) {

        JustZoom.init();

    }

}
