package de.keksuccino.justzoom.networking;

import de.keksuccino.justzoom.ClientZoomRules;
import de.keksuccino.justzoom.ServerZoomRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;

public final class FabricNetworking {

    private FabricNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ZoomRulesPayload.TYPE, ZoomRulesPayload.STREAM_CODEC);
        // Clients with Just Zoom announce its channel right after the login, while vanilla clients never do.
        // Only sending the rules to clients that announced the channel keeps Just Zoom optional on both sides.
        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if (channels.contains(ZoomRulesPayload.TYPE.id())) sender.sendPacket(ServerZoomRules.getRules());
        });
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ZoomRulesPayload.TYPE, (rules, context) -> ClientZoomRules.apply(rules));
    }

}
