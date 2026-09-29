package de.keksuccino.justzoom;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public enum ShowHudMode {

    @SerializedName("only_spyglass")
    ONLY_SPYGLASS("justzoom.options.show_hud.only_spyglass", true, false),
    @SerializedName("only_keybind_zoom")
    ONLY_KEYBIND_ZOOM("justzoom.options.show_hud.only_keybind_zoom", false, true),
    @SerializedName("spyglass_and_keybind_zoom")
    SPYGLASS_AND_KEYBIND_ZOOM("justzoom.options.show_hud.spyglass_and_keybind_zoom", true, true),
    @SerializedName("never")
    NEVER("justzoom.options.show_hud.never", false, false);

    private static final ShowHudMode[] VALUES = values();

    private final String translationKey;
    private final boolean shownForSpyglass;
    private final boolean shownForKeybindZoom;

    ShowHudMode(@NotNull String translationKey, boolean shownForSpyglass, boolean shownForKeybindZoom) {
        this.translationKey = translationKey;
        this.shownForSpyglass = shownForSpyglass;
        this.shownForKeybindZoom = shownForKeybindZoom;
    }

    @NotNull
    public String getTranslationKey() {
        return this.translationKey;
    }

    public boolean shouldShow(boolean spyglassScoping, boolean keybindZooming) {
        return spyglassScoping && this.shownForSpyglass || keybindZooming && this.shownForKeybindZoom;
    }

    public boolean shouldHide(boolean spyglassScoping, boolean keybindZooming) {
        return (spyglassScoping || keybindZooming) && !this.shouldShow(spyglassScoping, keybindZooming);
    }

    @NotNull
    public ShowHudMode next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

}
