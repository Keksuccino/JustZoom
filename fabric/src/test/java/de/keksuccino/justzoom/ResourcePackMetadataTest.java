package de.keksuccino.justzoom;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourcePackMetadataTest {

    @Test
    void bundledAssetsDeclareTheRunningMinecraftFormat() throws Exception {
        JsonObject gameFormats = readResource("/version.json").getAsJsonObject("pack_version");
        int resourceFormat = gameFormats.has("resource") ? gameFormats.get("resource").getAsInt() : gameFormats.get("resource_major").getAsInt();
        JsonObject pack = readResource("/pack.mcmeta").getAsJsonObject("pack");
        assertNotNull(pack.get("min_format"));
        assertEquals(resourceFormat, pack.get("min_format").getAsInt());
        // Modern metadata requires an upper field; keep all future formats allowed.
        assertEquals(Integer.MAX_VALUE, pack.get("max_format").getAsInt());
        if (resourceFormat < 65) {
            assertEquals(resourceFormat, pack.get("pack_format").getAsInt());
        }
    }

    private static JsonObject readResource(String path) throws Exception {
        try (var stream = ResourcePackMetadataTest.class.getResourceAsStream(path)) {
            assertNotNull(stream);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

}
