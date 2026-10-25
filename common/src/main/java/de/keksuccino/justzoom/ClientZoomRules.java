package de.keksuccino.justzoom;

import de.keksuccino.justzoom.networking.ZoomRulesPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * The zoom rules of the server the client is currently connected to.
 */
public final class ClientZoomRules {

    static final String STANDALONE_ZOOM_DISABLED_TRANSLATION_KEY = "justzoom.server.standalone_zoom_disabled";
    static final long WARNING_COOLDOWN_NANOS = 3_000_000_000L;

    private static final WarningCooldown WARNING_COOLDOWN = new WarningCooldown(System::nanoTime);
    // Depending on the loader, payload handlers run on the network thread, while zoom checks run on the render thread.
    private static volatile ZoomRulesPayload rules = ZoomRulesPayload.DEFAULT;

    private ClientZoomRules() {
    }

    public static boolean isStandaloneZoomAllowed() {
        return rules.allowStandaloneZoom();
    }

    /**
     * Applies the rules sent by the server. Only servers with Just Zoom send them.
     */
    public static void apply(@NotNull ZoomRulesPayload serverRules) {
        rules = Objects.requireNonNull(serverRules);
    }

    /**
     * Restores the default rules when joining a server or world, so servers without Just Zoom, which never send rules, allow everything.
     */
    public static void reset() {
        rules = ZoomRulesPayload.DEFAULT;
    }

    /**
     * Tells the player in chat that the server disabled standalone zoom when they press the zoom key.
     */
    public static void onZoomKeyPressed() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || isStandaloneZoomAllowed() || !WARNING_COOLDOWN.tryStart()) return;
        player.displayClientMessage(createStandaloneZoomDisabledWarning(), false);
    }

    @NotNull
    static Component createStandaloneZoomDisabledWarning() {
        return Component.translatable(STANDALONE_ZOOM_DISABLED_TRANSLATION_KEY).withStyle(ChatFormatting.GOLD);
    }

    /**
     * Keeps repeated zoom key presses from flooding the chat with the same warning.
     */
    static final class WarningCooldown {

        private final LongSupplier nanoTimeSource;
        private long lastWarningNanos;
        private boolean warningShown;

        WarningCooldown(@NotNull LongSupplier nanoTimeSource) {
            this.nanoTimeSource = Objects.requireNonNull(nanoTimeSource);
        }

        /**
         * Starts a new cooldown and returns true if no warning was shown during the current one.
         */
        boolean tryStart() {
            long nowNanos = this.nanoTimeSource.getAsLong();
            if (this.warningShown && nowNanos - this.lastWarningNanos < WARNING_COOLDOWN_NANOS) return false;
            this.lastWarningNanos = nowNanos;
            this.warningShown = true;
            return true;
        }

    }

}
