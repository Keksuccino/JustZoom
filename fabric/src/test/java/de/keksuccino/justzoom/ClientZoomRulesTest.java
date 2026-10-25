package de.keksuccino.justzoom;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.keksuccino.justzoom.networking.ZoomRulesPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientZoomRulesTest {

    @AfterEach
    void resetRules() {
        ClientZoomRules.reset();
    }

    @Test
    void allowsStandaloneZoomWithoutServerRules() {
        assertTrue(ClientZoomRules.isStandaloneZoomAllowed());
    }

    @Test
    void followsTheRulesSentByTheServer() {
        ClientZoomRules.apply(new ZoomRulesPayload(false));
        assertFalse(ClientZoomRules.isStandaloneZoomAllowed());

        ClientZoomRules.apply(new ZoomRulesPayload(true));
        assertTrue(ClientZoomRules.isStandaloneZoomAllowed());
    }

    @Test
    void resetAllowsStandaloneZoomAgainForServersWithoutJustZoom() {
        ClientZoomRules.apply(new ZoomRulesPayload(false));

        ClientZoomRules.reset();

        assertTrue(ClientZoomRules.isStandaloneZoomAllowed());
    }

    @Test
    void showsTheFirstWarningImmediately() {
        assertTrue(new ClientZoomRules.WarningCooldown(new TestNanoClock()).tryStart());
    }

    @Test
    void suppressesRepeatedWarningsDuringTheCooldown() {
        TestNanoClock clock = new TestNanoClock();
        ClientZoomRules.WarningCooldown cooldown = new ClientZoomRules.WarningCooldown(clock);
        cooldown.tryStart();

        clock.advanceNanos(ClientZoomRules.WARNING_COOLDOWN_NANOS - 1L);

        assertFalse(cooldown.tryStart());
    }

    @Test
    void showsTheWarningAgainOnceTheCooldownEnded() {
        TestNanoClock clock = new TestNanoClock();
        ClientZoomRules.WarningCooldown cooldown = new ClientZoomRules.WarningCooldown(clock);
        cooldown.tryStart();
        clock.advanceNanos(ClientZoomRules.WARNING_COOLDOWN_NANOS - 1L);
        cooldown.tryStart();

        clock.advanceNanos(1L);

        assertTrue(cooldown.tryStart());
        clock.advanceNanos(1L);
        assertFalse(cooldown.tryStart());
    }

    @Test
    void warnsInGoldWithATranslatedMessage() {
        Component warning = ClientZoomRules.createStandaloneZoomDisabledWarning();

        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GOLD), warning.getStyle().getColor());
        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, warning.getContents());
        assertEquals(ClientZoomRules.STANDALONE_ZOOM_DISABLED_TRANSLATION_KEY, contents.getKey());
    }

    @Test
    void everyLanguageTranslatesTheWarning() throws IOException {
        Path languageDirectory = Path.of("..", "common", "src", "main", "resources", "assets", "justzoom", "lang");
        List<Path> languageFiles;
        try (Stream<Path> files = Files.list(languageDirectory)) {
            languageFiles = files.filter(file -> file.toString().endsWith(".json")).toList();
        }

        assertFalse(languageFiles.isEmpty());
        for (Path languageFile : languageFiles) {
            JsonObject translations = JsonParser.parseString(Files.readString(languageFile, StandardCharsets.UTF_8)).getAsJsonObject();
            assertTrue(translations.has(ClientZoomRules.STANDALONE_ZOOM_DISABLED_TRANSLATION_KEY), languageFile::toString);
        }
    }

    private static final class TestNanoClock implements LongSupplier {

        private long nowNanos = 42L;

        @Override
        public long getAsLong() {
            return this.nowNanos;
        }

        void advanceNanos(long nanos) {
            this.nowNanos += nanos;
        }

    }

}
