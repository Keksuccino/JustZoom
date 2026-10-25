package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ClientZoomRules;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    // Forget the previous server's zoom rules on every login, including proxy server switches that keep the connection alive.
    // Servers with Just Zoom send their rules after the login, so this never discards the new server's rules.
    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void before_handleLogin_JustZoom(ClientboundLoginPacket packet, CallbackInfo info) {
        ClientZoomRules.reset();
    }

}
