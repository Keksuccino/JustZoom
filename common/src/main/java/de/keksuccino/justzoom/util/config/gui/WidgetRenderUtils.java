package de.keksuccino.justzoom.util.config.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

/** Alpha support for vanilla widgets whose older texture render paths ignore AbstractWidget.alpha. */
public final class WidgetRenderUtils {

    private WidgetRenderUtils() {
    }

    public static void withAlpha(@NotNull GuiGraphics graphics, float alpha, @NotNull Runnable draw) {
        if (alpha >= 1.0F) {
            draw.run();
            return;
        }
        // Flush buffered draws before changing shader color and before restoring it, so opacity
        // cannot affect previously queued draws or leak into another widget after an exception.
        graphics.flush();
        float[] color = RenderSystem.getShaderColor().clone();
        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3] * alpha);
        try {
            draw.run();
        } finally {
            graphics.flush();
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            if (!blending) RenderSystem.disableBlend();
        }
    }

    public static int multiplyAlpha(int color, float alpha) {
        int originalAlpha = color >>> 24;
        // Vanilla text fields also accept RGB-only colors and interpret them as opaque.
        if (originalAlpha == 0) originalAlpha = 255;
        int scaledAlpha = Math.round(originalAlpha * Math.max(0.0F, Math.min(1.0F, alpha)));
        return color & 0x00FFFFFF | scaledAlpha << 24;
    }

}
