package de.keksuccino.justzoom.networking;

import de.keksuccino.justzoom.JustZoom;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The zoom rules a server with Just Zoom sends to joining players that have Just Zoom installed.
 */
public record ZoomRulesPayload(boolean allowStandaloneZoom) {

    /** The rules of servers without Just Zoom, which never send any. */
    public static final ZoomRulesPayload DEFAULT = new ZoomRulesPayload(true);
    public static final ResourceLocation ID = new ResourceLocation(JustZoom.MOD_ID, "zoom_rules");

    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.allowStandaloneZoom);
    }

    @NotNull
    public static ZoomRulesPayload read(@NotNull FriendlyByteBuf buffer) {
        boolean allowStandaloneZoom = buffer.readBoolean();
        // Newer versions may append more rules. Skipping unknown trailing data keeps older clients compatible with newer servers.
        buffer.skipBytes(buffer.readableBytes());
        return new ZoomRulesPayload(allowStandaloneZoom);
    }

}
