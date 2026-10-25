package de.keksuccino.justzoom.networking;

import de.keksuccino.justzoom.JustZoom;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * The zoom rules a server with Just Zoom sends to joining players that have Just Zoom installed.
 */
public record ZoomRulesPayload(boolean allowStandaloneZoom) implements CustomPacketPayload {

    /** The rules of servers without Just Zoom, which never send any. */
    public static final ZoomRulesPayload DEFAULT = new ZoomRulesPayload(true);
    public static final CustomPacketPayload.Type<ZoomRulesPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(JustZoom.MOD_ID, "zoom_rules"));
    public static final StreamCodec<FriendlyByteBuf, ZoomRulesPayload> STREAM_CODEC = CustomPacketPayload.codec(ZoomRulesPayload::write, ZoomRulesPayload::read);

    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.allowStandaloneZoom);
    }

    @NotNull
    public static ZoomRulesPayload read(@NotNull FriendlyByteBuf buffer) {
        boolean allowStandaloneZoom = buffer.readBoolean();
        // Newer versions may append more rules. Vanilla disconnects when a payload is not read completely,
        // so skipping unknown trailing data keeps older clients compatible with newer servers.
        buffer.skipBytes(buffer.readableBytes());
        return new ZoomRulesPayload(allowStandaloneZoom);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<ZoomRulesPayload> type() {
        return TYPE;
    }

}
