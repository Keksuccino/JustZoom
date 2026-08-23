package de.keksuccino.justzoom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class OpenOptionsToastHandler {

    @Nullable
    private static TutorialToast activeToast;
    private static boolean shownThisSession;

    private OpenOptionsToastHandler() {
    }

    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() != null || minecraft.player == null) return;

        boolean zoomKeyDown = KeyMappings.KEY_TOGGLE_ZOOM.isDown();
        boolean spyglassScoping = minecraft.player.isScoping();
        PersistenceData persistenceData = JustZoom.getPersistenceData();
        boolean alreadyShown = shownThisSession || persistenceData.openSettingsToastShown.getValueOrDefault(false);
        if (!shouldShow(alreadyShown, zoomKeyDown, spyglassScoping)) return;

        // Vanilla tutorial toasts have no timeout when they are neither timed nor progressable, so hide() is the only dismissal path.
        TutorialToast toast = new TutorialToast(minecraft.font, TutorialToast.Icons.RECIPE_BOOK, Component.translatable("justzoom.options"), Component.translatable("justzoom.open_settings_toast", KeyMappings.KEY_OPEN_OPTIONS.getTranslatedKeyMessage()), false);
        activeToast = toast;
        shownThisSession = true;
        minecraft.gui.toastManager().addToast(toast);
        persistenceData.openSettingsToastShown.setValue(true);
    }

    public static void onOpenOptionsKeyPressed() {
        if (activeToast == null) return;
        activeToast.hide();
        activeToast = null;
    }

    static boolean shouldShow(boolean alreadyShown, boolean zoomKeyDown, boolean spyglassScoping) {
        return !alreadyShown && (zoomKeyDown || spyglassScoping);
    }

}
