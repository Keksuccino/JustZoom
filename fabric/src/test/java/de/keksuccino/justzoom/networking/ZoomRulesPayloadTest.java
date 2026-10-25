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

        new ZoomRulesPayload(allowStandaloneZoom).write(buffer);
        ZoomRulesPayload decoded = ZoomRulesPayload.read(buffer);

        assertEquals(allowStandaloneZoom, decoded.allowStandaloneZoom());
        assertFalse(buffer.isReadable());
    }

    @Test
    void consumesRulesAddedByNewerVersions() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        new ZoomRulesPayload(false).write(buffer);
        buffer.writeVarInt(1234);
        buffer.writeBoolean(true);

        ZoomRulesPayload decoded = ZoomRulesPayload.read(buffer);

        assertFalse(decoded.allowStandaloneZoom());
        assertFalse(buffer.isReadable());
    }

    @Test
    void defaultRulesAllowStandaloneZoom() {
        assertTrue(ZoomRulesPayload.DEFAULT.allowStandaloneZoom());
    }

    @Test
    void usesTheJustZoomNamespace() {
        assertEquals("justzoom", ZoomRulesPayload.ID.getNamespace());
    }

}
