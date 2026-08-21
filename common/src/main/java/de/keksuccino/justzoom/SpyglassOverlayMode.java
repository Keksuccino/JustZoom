package de.keksuccino.justzoom;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public enum SpyglassOverlayMode {

    @SerializedName("only_spyglass")
    ONLY_SPYGLASS("justzoom.options.spyglass_overlay.only_spyglass", true, false),
    @SerializedName("only_keybind_zoom")
    ONLY_KEYBIND_ZOOM("justzoom.options.spyglass_overlay.only_keybind_zoom", false, true),
    @SerializedName("spyglass_and_keybind_zoom")
    SPYGLASS_AND_KEYBIND_ZOOM("justzoom.options.spyglass_overlay.spyglass_and_keybind_zoom", true, true),
    @SerializedName("disabled")
    DISABLED("justzoom.options.spyglass_overlay.disabled", false, false);

    private static final SpyglassOverlayMode[] VALUES = values();

    private final String translationKey;
    private final boolean shownForSpyglass;
    private final boolean shownForKeybindZoom;

    SpyglassOverlayMode(@NotNull String translationKey, boolean shownForSpyglass, boolean shownForKeybindZoom) {
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

    @NotNull
    public SpyglassOverlayMode next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

}
