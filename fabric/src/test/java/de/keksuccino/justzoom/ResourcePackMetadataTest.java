package de.keksuccino.justzoom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void bundledMetadataIsAValidResourcePackForTheRunningMinecraftFormat() throws Exception {
        int resourceFormat = readResource("/version.json").getAsJsonObject("pack_version").get("resource_major").getAsInt();

        DataResult<InclusiveRange<PackFormat>> formats = decodeFormats(readResource("/pack.mcmeta").getAsJsonObject("pack"), PackType.CLIENT_RESOURCES);

        assertTrue(formats.isSuccess(), () -> formats.error().orElseThrow().message());
        assertTrue(formats.getOrThrow().isValueInRange(PackFormat.of(resourceFormat)));
    }

    // NeoForge also reads mod packs as data packs, which is why its jar ships a separate pack.mcmeta.
    @Test
    void bundledMetadataCannotAlsoDescribeADataPack() throws Exception {
        assertFalse(decodeFormats(readResource("/pack.mcmeta").getAsJsonObject("pack"), PackType.SERVER_DATA).isSuccess());
    }

    @Test
    void neoForgeMetadataDeclaresNoFormatsSoNeoForgeAcceptsItForEveryPackType() throws Exception {
        JsonObject pack = JsonParser.parseString(Files.readString(Path.of("..", "neoforge", "src", "main", "resources", "pack.mcmeta"), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("pack");

        assertNotNull(pack.get("description"));
        for (String formatField : List.of("min_format", "max_format", "pack_format", "supported_formats")) {
            assertFalse(pack.has(formatField), formatField);
        }
    }

    private static DataResult<InclusiveRange<PackFormat>> decodeFormats(JsonObject pack, PackType packType) {
        return PackFormat.packCodec(packType).decode(JsonOps.INSTANCE, JsonOps.INSTANCE.getMap((JsonElement) pack).getOrThrow());
    }

    private static JsonObject readResource(String path) throws Exception {
        try (var stream = ResourcePackMetadataTest.class.getResourceAsStream(path)) {
            assertNotNull(stream);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

}
