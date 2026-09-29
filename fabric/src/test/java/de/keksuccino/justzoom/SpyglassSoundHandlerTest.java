package de.keksuccino.justzoom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SpyglassSoundHandlerTest {

    @Test
    void emitsOneSoundAtEachEnabledKeybindZoomBoundary() {
        SpyglassSoundHandler.KeybindZoomSoundState state = new SpyglassSoundHandler.KeybindZoomSoundState();

        assertSame(SpyglassSoundHandler.KeybindZoomSoundTransition.START, state.update(true, true));
        assertNull(state.update(true, true));
        assertSame(SpyglassSoundHandler.KeybindZoomSoundTransition.STOP, state.update(false, true));
        assertNull(state.update(false, true));
    }

    @Test
    void staysSilentAcrossDisabledKeybindZoomBoundaries() {
        SpyglassSoundHandler.KeybindZoomSoundState state = new SpyglassSoundHandler.KeybindZoomSoundState();

        assertNull(state.update(true, false));
        assertNull(state.update(false, false));
    }

    @Test
    void doesNotEmitAnUnpairedStopWhenSoundsAreEnabledMidZoom() {
        SpyglassSoundHandler.KeybindZoomSoundState state = new SpyglassSoundHandler.KeybindZoomSoundState();

        assertNull(state.update(true, false));
        assertNull(state.update(true, true));
        assertNull(state.update(false, true));
    }

    @Test
    void disablingSoundsMidZoomSuppressesTheStopSound() {
        SpyglassSoundHandler.KeybindZoomSoundState state = new SpyglassSoundHandler.KeybindZoomSoundState();

        assertSame(SpyglassSoundHandler.KeybindZoomSoundTransition.START, state.update(true, true));
        assertNull(state.update(false, false));
    }

}
