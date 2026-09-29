package de.keksuccino.justzoom.util;

import net.minecraft.client.gui.GuiGraphics;

/** Bridge to the vanilla overlay when an older loader replaces the complete HUD renderer. */
public interface SpyglassOverlayRenderer {

    void renderZoomOverlay_JustZoom(GuiGraphics graphics);

}
