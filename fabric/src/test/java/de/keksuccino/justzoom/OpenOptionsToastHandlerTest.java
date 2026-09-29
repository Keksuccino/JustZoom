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
    void toastContainsOnlyBlackInstructionTextWithABoldTitleColoredKey() {
        Component toastText = OpenOptionsToastHandler.createToastText(Component.literal("B"));

        assertEquals(1, toastText.getSiblings().size());
        Component instruction = toastText.getSiblings().getFirst();
        TextColor instructionColor = instruction.getStyle().getColor();
        assertNotNull(instructionColor);
        assertEquals(OpenOptionsToastHandler.TOAST_TEXT_COLOR, instructionColor.getValue());

        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, instruction.getContents());
        assertEquals(1, contents.getArgs().length);
        Component keyName = assertInstanceOf(Component.class, contents.getArgs()[0]);
        TextColor keyColor = keyName.getStyle().getColor();
        assertNotNull(keyColor);
        assertEquals(OpenOptionsToastHandler.TUTORIAL_TITLE_COLOR, keyColor.getValue());
        assertTrue(keyName.getStyle().isBold());
    }

}
