package de.keksuccino.justzoom;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimedTutorialToastTest {

    private static final int DISPLAY_TIME_MS = 30_000;

    // Toast.Visibility references sound events from the built-in registries.
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void staysVisibleUntilTheDisplayTimeIsOver() {
        TimedTutorialToast toast = createToast(1);

        assertEquals(Toast.Visibility.SHOW, toast.getVisibility(0L));
        assertEquals(Toast.Visibility.SHOW, toast.getVisibility(DISPLAY_TIME_MS));
    }

    @Test
    void hidesItselfOnceTheDisplayTimeIsOver() {
        TimedTutorialToast toast = createToast(1);

        assertEquals(Toast.Visibility.HIDE, toast.getVisibility(DISPLAY_TIME_MS + 1L));
        assertEquals(Toast.Visibility.HIDE, toast.getVisibility(0L));
    }

    @Test
    void hidingDismissesTheToastBeforeTheDisplayTimeIsOver() {
        TimedTutorialToast toast = createToast(1);

        toast.hide();

        assertEquals(Toast.Visibility.HIDE, toast.getVisibility(0L));
    }

    @Test
    void upToTwoLinesKeepTheVanillaTutorialToastSize() {
        for (int lineCount = 0; lineCount <= 2; lineCount++) {
            TimedTutorialToast toast = createToast(lineCount);
            assertEquals(160, toast.width());
            assertEquals(32, toast.height());
            assertEquals(1, toast.slotCount());
        }
    }

    @Test
    void additionalLinesGrowTheToastAndItsOccupiedSlots() {
        TimedTutorialToast threeLines = createToast(3);
        TimedTutorialToast fourLines = createToast(4);

        assertEquals(43, threeLines.height());
        assertEquals(2, threeLines.slotCount());
        assertEquals(54, fourLines.height());
        assertEquals(2, fourLines.slotCount());
    }

    private static TimedTutorialToast createToast(int lineCount) {
        return new TimedTutorialToast(TutorialToast.Icons.RECIPE_BOOK, Component.empty(), Collections.nCopies(lineCount, FormattedCharSequence.forward("line", Style.EMPTY)), DISPLAY_TIME_MS);
    }

}
