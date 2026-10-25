package de.keksuccino.justzoom.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoomRulesPayloadTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void keepsTheStandaloneZoomRuleThroughEncoding(boolean allowStandaloneZoom) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ZoomRulesPayload.STREAM_CODEC.encode(buffer, new ZoomRulesPayload(allowStandaloneZoom));
        ZoomRulesPayload decoded = ZoomRulesPayload.STREAM_CODEC.decode(buffer);

        assertEquals(allowStandaloneZoom, decoded.allowStandaloneZoom());
        assertFalse(buffer.isReadable());
    }

    @Test
    void consumesRulesAddedByNewerVersions() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ZoomRulesPayload.STREAM_CODEC.encode(buffer, new ZoomRulesPayload(false));
        buffer.writeVarInt(1234);
        buffer.writeBoolean(true);

        ZoomRulesPayload decoded = ZoomRulesPayload.STREAM_CODEC.decode(buffer);

        assertFalse(decoded.allowStandaloneZoom());
        assertFalse(buffer.isReadable());
    }

    @Test
    void defaultRulesAllowStandaloneZoom() {
        assertTrue(ZoomRulesPayload.DEFAULT.allowStandaloneZoom());
    }

    @Test
    void usesTheJustZoomNamespace() {
        assertEquals("justzoom", ZoomRulesPayload.TYPE.id().getNamespace());
        assertEquals(ZoomRulesPayload.TYPE, ZoomRulesPayload.DEFAULT.type());
    }

}
