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
     * @reason Initialize server-side configuration after dedicated server setup has completed.
     */
    @Inject(method = "initServer", at = @At("RETURN"))
    private void after_initServer_JustZoom(CallbackInfoReturnable<Boolean> info) {

        if (!Services.PLATFORM.isOnClient()) {
            JustZoom.init();
        }

    }

}
