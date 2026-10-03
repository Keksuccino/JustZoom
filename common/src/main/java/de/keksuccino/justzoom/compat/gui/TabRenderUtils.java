package de.keksuccino.justzoom.compat.gui;

import com.mojang.blaze3d.vertex.PoseStack;

/** Recreates the modern menu tab's simple borders with fills, without bundling Minecraft textures. */
public final class TabRenderUtils {

    private TabRenderUtils() {
    }

    public static void drawFrame(PoseStack graphics, int x, int y, int width, int height, boolean selected, boolean highlighted, float alpha) {
        if (width < 4 || height < 8 || alpha <= 0.0F) return;
        int top = y + (selected ? 0 : 4);
        int bottom = y + height;
        int shadow = WidgetRenderUtils.multiplyAlpha(0xBF000000, alpha);
        int line = WidgetRenderUtils.multiplyAlpha(highlighted ? 0xFFFFFFFF : 0x33FFFFFF, alpha);
        int separator = WidgetRenderUtils.multiplyAlpha(0x33FFFFFF, alpha);
        fill(graphics, x, top, x + width, top + 1, shadow);
        fill(graphics, x, top + 1, x + 1, bottom - 2, shadow);
        fill(graphics, x + width - 1, top + 1, x + width, bottom - 2, shadow);
        fill(graphics, x + 1, top + 1, x + width - 1, top + 2, line);
        fill(graphics, x + 1, top + 2, x + 2, bottom - 2, line);
        fill(graphics, x + width - 2, top + 2, x + width - 1, bottom - 2, line);
        if (selected) {
            // Match the content shading so the selected tab joins the body without a bottom border.
            drawContentBackground(graphics, x + 2, top + 2, width - 4, height - 2, alpha);
            fill(graphics, x, bottom - 2, x + 1, bottom - 1, separator);
            fill(graphics, x + 1, bottom - 2, x + 2, bottom - 1, line);
            fill(graphics, x + width - 2, bottom - 2, x + width - 1, bottom - 1, line);
            fill(graphics, x + width - 1, bottom - 2, x + width, bottom - 1, separator);
            fill(graphics, x, bottom - 1, x + 2, bottom, shadow);
            fill(graphics, x + width - 2, bottom - 1, x + width, bottom, shadow);
        } else {
            fill(graphics, x + 2, top + 2, x + width - 2, bottom - 2, WidgetRenderUtils.multiplyAlpha(0xDB000000, alpha));
            if (highlighted) fill(graphics, x + 1, bottom - 3, x + width - 1, bottom - 2, line);
            fill(graphics, x, bottom - 2, x + width, bottom - 1, separator);
            fill(graphics, x, bottom - 1, x + width, bottom, shadow);
        }
    }

    public static void drawSeparator(PoseStack graphics, int left, int right, int y, float alpha) {
        drawSeparator(graphics, left, right, y, alpha, false);
    }

    public static void drawFooterSeparator(PoseStack graphics, int left, int right, int y, float alpha) {
        drawSeparator(graphics, left, right, y, alpha, true);
    }

    /** Mirrors modern menu shading while retaining the host game's background. */
    public static void drawContentBackground(PoseStack graphics, int x, int y, int width, int height, float alpha) {
        if (width <= 0 || height <= 0 || alpha <= 0.0F) return;
        fill(graphics, x, y, x + width, y + height, WidgetRenderUtils.multiplyAlpha(0x40000000, alpha));
    }

    private static void drawSeparator(PoseStack graphics, int left, int right, int y, float alpha, boolean footer) {
        if (right <= left || alpha <= 0.0F) return;
        fill(graphics, left, y, right, y + 1, WidgetRenderUtils.multiplyAlpha(footer ? 0xBF000000 : 0x33FFFFFF, alpha));
        fill(graphics, left, y + 1, right, y + 2, WidgetRenderUtils.multiplyAlpha(footer ? 0x33FFFFFF : 0xBF000000, alpha));
    }

    private static void fill(PoseStack graphics, int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.GuiComponent.fill(graphics, left, top, right, bottom, color);
    }

}
