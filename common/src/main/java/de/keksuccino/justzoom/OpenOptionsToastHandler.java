package de.keksuccino.justzoom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OpenOptionsToastHandler {

    static final int TUTORIAL_TITLE_COLOR = 0x500050;
    static final int TOAST_TEXT_COLOR = 0x000000;

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

        Component toastText = createToastText(KeyMappings.KEY_OPEN_OPTIONS.getTranslatedKeyMessage());
        // Vanilla tutorial toasts have no timeout when they are neither timed nor progressable, so hide() is the only dismissal path.
        TutorialToast toast = new TutorialToast(minecraft.font, TutorialToast.Icons.RECIPE_BOOK, toastText, null, false);
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

    @NotNull
    static Component createToastText(@NotNull Component keyName) {
        Component styledKeyName = keyName.copy().withStyle(Style.EMPTY.withColor(TUTORIAL_TITLE_COLOR).withBold(true));
        Component instruction = Component.translatable("justzoom.open_settings_toast", styledKeyName).withStyle(Style.EMPTY.withColor(TOAST_TEXT_COLOR));
        // TutorialToast colors its root like a title. Nesting the explicitly colored instruction keeps the sentence black while preserving the title color only on the key.
        return Component.empty().append(instruction);
    }

}
