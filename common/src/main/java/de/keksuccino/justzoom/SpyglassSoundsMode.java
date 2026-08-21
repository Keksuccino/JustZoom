package de.keksuccino.justzoom;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public enum SpyglassSoundsMode {

    @SerializedName("only_spyglass")
    ONLY_SPYGLASS("justzoom.options.spyglass_sounds.only_spyglass", true, false),
    @SerializedName("only_keybind_zoom")
    ONLY_KEYBIND_ZOOM("justzoom.options.spyglass_sounds.only_keybind_zoom", false, true),
    @SerializedName("spyglass_and_keybind_zoom")
    SPYGLASS_AND_KEYBIND_ZOOM("justzoom.options.spyglass_sounds.spyglass_and_keybind_zoom", true, true),
    @SerializedName("disabled")
    DISABLED("justzoom.options.spyglass_sounds.disabled", false, false);

    private static final SpyglassSoundsMode[] VALUES = values();

    private final String translationKey;
    private final boolean playedForSpyglass;
    private final boolean playedForKeybindZoom;

    SpyglassSoundsMode(@NotNull String translationKey, boolean playedForSpyglass, boolean playedForKeybindZoom) {
        this.translationKey = translationKey;
        this.playedForSpyglass = playedForSpyglass;
        this.playedForKeybindZoom = playedForKeybindZoom;
    }

    @NotNull
    public String getTranslationKey() {
        return this.translationKey;
    }

    public boolean shouldPlayForSpyglass() {
        return this.playedForSpyglass;
    }

    public boolean shouldPlayForKeybindZoom() {
        return this.playedForKeybindZoom;
    }

    @NotNull
    public SpyglassSoundsMode next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

}
