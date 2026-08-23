package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

}
