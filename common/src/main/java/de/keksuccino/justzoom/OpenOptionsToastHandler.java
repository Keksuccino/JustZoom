package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OpenOptionsToastHandler {

    static final int TUTORIAL_TITLE_COLOR = 0x500050;
    static final int TOAST_TEXT_COLOR = 0x000000;
    static final int DISPLAY_TIME_MS = 30_000;
    static final String OPEN_WITH_KEY_TRANSLATION_KEY = "justzoom.open_settings_toast";
    static final String OPEN_WITH_MOD_MENU_TRANSLATION_KEY = "justzoom.open_settings_toast.mod_menu";
    static final String MOD_MENU_NAME_TRANSLATION_KEY = "justzoom.open_settings_toast.mod_menu.name";
    static final String OPEN_WITH_MODS_MENU_TRANSLATION_KEY = "justzoom.open_settings_toast.mods_menu";
    // Provided by (Neo)Forge itself, so the hint names the Mods menu exactly like the loader's own button does.
    static final String LOADER_MODS_MENU_NAME_TRANSLATION_KEY = "fml.menu.mods";

    @Nullable
    private static TutorialToast activeToast;
    private static boolean shownThisSession;

    private OpenOptionsToastHandler() {
    }

    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null) return;

        boolean zoomKeyDown = KeyMappings.KEY_TOGGLE_ZOOM.isDown();
        boolean spyglassScoping = minecraft.player.isScoping();
        PersistenceData persistenceData = JustZoom.getPersistenceData();
        boolean alreadyShown = shownThisSession || persistenceData.openSettingsToastShown.getValueOrDefault(false);
        if (!shouldShow(alreadyShown, zoomKeyDown, spyglassScoping)) return;

        KeyMapping openOptionsKey = KeyMappings.KEY_OPEN_OPTIONS;
        Component toastText = createToastText(openOptionsKey.isUnbound() ? null : openOptionsKey.getTranslatedKeyMessage(), Services.PLATFORM.hasBuiltInModList());
        TutorialToast toast = createToast(minecraft.font, toastText);
        activeToast = toast;
        shownThisSession = true;
        minecraft.getToastManager().addToast(toast);
        persistenceData.openSettingsToastShown.setValue(true);
    }

    public static void dismissToast() {
        if (activeToast == null) return;
        activeToast.hide();
        activeToast = null;
    }

    static boolean shouldShow(boolean alreadyShown, boolean zoomKeyDown, boolean spyglassScoping) {
        return !alreadyShown && (zoomKeyDown || spyglassScoping);
    }

    @NotNull
    static TutorialToast createToast(@NotNull Font font, @NotNull Component toastText) {
        return new TutorialToast(font, TutorialToast.Icons.RECIPE_BOOK, toastText, null, false, DISPLAY_TIME_MS);
    }

    /**
     * @param openOptionsKeyName The name of the bound open-options key, or {@code null} if that key is unbound.
     * @param builtInModList If the loader has a built-in mod list that can open the options screen.
     */
    @NotNull
    static Component createToastText(@Nullable Component openOptionsKeyName, boolean builtInModList) {
        if (openOptionsKeyName != null) return createInstructionText(OPEN_WITH_KEY_TRANSLATION_KEY, openOptionsKeyName);
        // Without a bound key, the options screen is only reachable through a mod list, which Fabric only has with Mod Menu installed.
        if (builtInModList) return createInstructionText(OPEN_WITH_MODS_MENU_TRANSLATION_KEY, Component.translatable(LOADER_MODS_MENU_NAME_TRANSLATION_KEY));
        return createInstructionText(OPEN_WITH_MOD_MENU_TRANSLATION_KEY, Component.translatable(MOD_MENU_NAME_TRANSLATION_KEY));
    }

    @NotNull
    private static Component createInstructionText(@NotNull String translationKey, @NotNull Component highlightedName) {
        Component styledName = highlightedName.copy().withStyle(Style.EMPTY.withColor(TUTORIAL_TITLE_COLOR).withBold(true));
        Component instruction = Component.translatable(translationKey, styledName).withStyle(Style.EMPTY.withColor(TOAST_TEXT_COLOR));
        // TutorialToast colors its root like a title. Nesting the explicitly colored instruction keeps the sentence black while preserving the title color only on the highlighted name.
        return Component.empty().append(instruction);
    }

}
