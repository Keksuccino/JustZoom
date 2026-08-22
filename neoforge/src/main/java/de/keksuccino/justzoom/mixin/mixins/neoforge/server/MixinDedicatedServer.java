package de.keksuccino.justzoom.mixin.mixins.neoforge.server;

import de.keksuccino.justzoom.JustZoom;
import de.keksuccino.justzoom.platform.Services;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class MixinDedicatedServer {

    /**
     * @reason This is to correctly call the Just Zoom init stuff on the server-side without having to use NeoForge's hell of a server setup cycle system.
     */
    @Inject(method = "initServer", at = @At("RETURN"))
    private void return_initServer_JustZoom(CallbackInfoReturnable<Boolean> info) {

        if (!Services.PLATFORM.isOnClient()) {
            JustZoom.init();
        }

    }

}
