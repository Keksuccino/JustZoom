package de.keksuccino.justzoom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenOptionsToastHandlerTest {

    @Test
    void firstZoomKeyUseShowsToast() {
        assertTrue(OpenOptionsToastHandler.shouldShow(false, true, false));
    }

    @Test
    void firstSpyglassUseShowsToast() {
        assertTrue(OpenOptionsToastHandler.shouldShow(false, false, true));
    }

    @Test
    void idleGameplayDoesNotShowToast() {
        assertFalse(OpenOptionsToastHandler.shouldShow(false, false, false));
    }

    @Test
    void toastNeverShowsAgainAfterBeingRecorded() {
        assertFalse(OpenOptionsToastHandler.shouldShow(true, true, false));
        assertFalse(OpenOptionsToastHandler.shouldShow(true, false, true));
    }

    @Test
    void boundKeyToastContainsOnlyBlackInstructionTextWithABoldTitleColoredKey() {
        Component keyName = Component.literal("B");

        for (boolean builtInModList : new boolean[] { false, true }) {
            Component toastText = OpenOptionsToastHandler.createToastText(keyName, builtInModList);
            Component highlightedName = assertHighlightedInstruction(toastText, OpenOptionsToastHandler.OPEN_WITH_KEY_TRANSLATION_KEY);
            assertEquals(keyName.getContents(), highlightedName.getContents());
        }
    }

    @Test
    void unboundKeyToastPointsToModMenuWithoutABuiltInModList() {
        Component toastText = OpenOptionsToastHandler.createToastText(null, false);

        Component highlightedName = assertHighlightedInstruction(toastText, OpenOptionsToastHandler.OPEN_WITH_MOD_MENU_TRANSLATION_KEY);
        TranslatableContents nameContents = assertInstanceOf(TranslatableContents.class, highlightedName.getContents());
        assertEquals(OpenOptionsToastHandler.MOD_MENU_NAME_TRANSLATION_KEY, nameContents.getKey());
    }

    @Test
    void unboundKeyToastPointsToTheLoaderModsMenuWithABuiltInModList() {
        Component toastText = OpenOptionsToastHandler.createToastText(null, true);

        Component highlightedName = assertHighlightedInstruction(toastText, OpenOptionsToastHandler.OPEN_WITH_MODS_MENU_TRANSLATION_KEY);
        TranslatableContents nameContents = assertInstanceOf(TranslatableContents.class, highlightedName.getContents());
        assertEquals(OpenOptionsToastHandler.LOADER_MODS_MENU_NAME_TRANSLATION_KEY, nameContents.getKey());
    }

    private static Component assertHighlightedInstruction(Component toastText, String expectedTranslationKey) {
        assertEquals(1, toastText.getSiblings().size());
        Component instruction = toastText.getSiblings().getFirst();
        TextColor instructionColor = instruction.getStyle().getColor();
        assertNotNull(instructionColor);
        assertEquals(OpenOptionsToastHandler.TOAST_TEXT_COLOR, instructionColor.getValue());

        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, instruction.getContents());
        assertEquals(expectedTranslationKey, contents.getKey());
        assertEquals(1, contents.getArgs().length);
        Component highlightedName = assertInstanceOf(Component.class, contents.getArgs()[0]);
        TextColor nameColor = highlightedName.getStyle().getColor();
        assertNotNull(nameColor);
        assertEquals(OpenOptionsToastHandler.TUTORIAL_TITLE_COLOR, nameColor.getValue());
        assertTrue(highlightedName.getStyle().isBold());
        return highlightedName;
    }

}
