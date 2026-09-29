package de.keksuccino.justzoom;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SpyglassSoundHandler {

    private static final KeybindZoomSoundState KEYBIND_ZOOM_SOUND_STATE = new KeybindZoomSoundState();

    private SpyglassSoundHandler() {
    }

    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean keybindZooming = minecraft.player != null && ZoomHandler.isKeybindZooming();
        boolean soundsEnabled = JustZoom.getOptions().spyglassSounds.getValue().shouldPlayForKeybindZoom();
        KeybindZoomSoundTransition transition = KEYBIND_ZOOM_SOUND_STATE.update(keybindZooming, soundsEnabled);
        if (transition == null || minecraft.player == null) return;
        minecraft.player.playSound(transition == KeybindZoomSoundTransition.START ? SoundEvents.SPYGLASS_USE : SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
    }

    /**
     * Client mixins also wrap integrated-server item calls. Comparing entity identity keeps the setting local and preserves sounds broadcast by the server and sounds made by other players.
     */
    public static boolean shouldPlaySpyglassSounds(@NotNull LivingEntity entity) {
        return entity != Minecraft.getInstance().player || JustZoom.getOptions().spyglassSounds.getValue().shouldPlayForSpyglass();
    }

    enum KeybindZoomSoundTransition {

        START,
        STOP

    }

    static final class KeybindZoomSoundState {

        private boolean keybindZooming;
        private boolean soundSequenceActive;

        @Nullable
        KeybindZoomSoundTransition update(boolean keybindZooming, boolean soundsEnabled) {
            if (keybindZooming == this.keybindZooming) return null;
            this.keybindZooming = keybindZooming;
            if (keybindZooming) {
                this.soundSequenceActive = soundsEnabled;
                return soundsEnabled ? KeybindZoomSoundTransition.START : null;
            }

            boolean shouldPlayStop = this.soundSequenceActive && soundsEnabled;
            this.soundSequenceActive = false;
            return shouldPlayStop ? KeybindZoomSoundTransition.STOP : null;
        }

    }

}
