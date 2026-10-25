package de.keksuccino.justzoom.networking;

import de.keksuccino.justzoom.ClientZoomRules;
import de.keksuccino.justzoom.ServerZoomRules;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;

public final class NeoForgeNetworking {

    private static final String PROTOCOL_VERSION = "1";

    private NeoForgeNetworking() {
    }

    public static void register(@NotNull IEventBus modEventBus) {
        modEventBus.addListener(NeoForgeNetworking::onRegisterPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(NeoForgeNetworking::onPlayerLoggedIn);
    }

    private static void onRegisterPayloadHandlers(@NotNull RegisterPayloadHandlersEvent event) {
        // An optional channel keeps Just Zoom optional on both sides, so players can join servers with or without it.
        event.registrar(PROTOCOL_VERSION).optional().playToClient(ZoomRulesPayload.TYPE, ZoomRulesPayload.STREAM_CODEC, (rules, context) -> ClientZoomRules.apply(rules));
    }

    private static void onPlayerLoggedIn(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.connection.hasChannel(ZoomRulesPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, ServerZoomRules.getRules());
        }
    }

}
