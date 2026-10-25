package de.keksuccino.justzoom.networking;

import de.keksuccino.justzoom.ClientZoomRules;
import de.keksuccino.justzoom.ServerZoomRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.minecraft.network.FriendlyByteBuf;

public final class FabricNetworking {

    private FabricNetworking() {
    }

    public static void register() {
        // Clients with Just Zoom announce its channel right after the login, while vanilla clients never do.
        // Only sending the rules to clients that announced the channel keeps Just Zoom optional on both sides.
        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (!channels.contains(ZoomRulesPayload.ID)) return;
            FriendlyByteBuf buffer = PacketByteBufs.create();
            ServerZoomRules.getRules().write(buffer);
            sender.sendPacket(ZoomRulesPayload.ID, buffer);
        });
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ZoomRulesPayload.ID, (client, handler, buffer, responseSender) -> {
            ZoomRulesPayload rules = ZoomRulesPayload.read(buffer);
            // The receiver runs on the network thread, so apply the rules on the render thread like vanilla packets.
            client.execute(() -> ClientZoomRules.apply(rules));
        });
    }

}
