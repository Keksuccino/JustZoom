package de.keksuccino.justzoom.networking;

import de.keksuccino.justzoom.ClientZoomRules;
import de.keksuccino.justzoom.ServerZoomRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

public final class ForgeNetworking {

    private static final String PROTOCOL_VERSION = "1";
    // Accepting a missing channel on both sides keeps Just Zoom optional, so players can join servers with or without it.
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ZoomRulesPayload.ID, () -> PROTOCOL_VERSION, NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION), NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION));

    private ForgeNetworking() {
    }

    public static void register() {
        CHANNEL.messageBuilder(ZoomRulesPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(ZoomRulesPayload::write).decoder(ZoomRulesPayload::read).consumerMainThread((rules, context) -> ClientZoomRules.apply(rules)).add();
        MinecraftForge.EVENT_BUS.addListener(ForgeNetworking::onPlayerLoggedIn);
    }

    private static void onPlayerLoggedIn(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && CHANNEL.isRemotePresent(player.connection.connection)) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), ServerZoomRules.getRules());
        }
    }

}
