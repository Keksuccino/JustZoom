package de.keksuccino.justzoom.compat.gui;


/** Alpha support for vanilla widgets whose older texture render paths ignore AbstractWidget.alpha. */
public final class WidgetRenderUtils {

    private WidgetRenderUtils() {
    }

    public static int multiplyAlpha(int color, float alpha) {
        int originalAlpha = color >>> 24;
        // Vanilla text fields also accept RGB-only colors and interpret them as opaque.
        if (originalAlpha == 0) originalAlpha = 255;
        int scaledAlpha = Math.round(originalAlpha * Math.max(0.0F, Math.min(1.0F, alpha)));
        return color & 0x00FFFFFF | scaledAlpha << 24;
    }

}
