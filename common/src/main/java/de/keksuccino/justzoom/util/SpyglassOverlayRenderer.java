package de.keksuccino.justzoom.util;

import com.mojang.blaze3d.vertex.PoseStack;

/** Bridge to the vanilla overlay when an older loader replaces the complete HUD renderer. */
public interface SpyglassOverlayRenderer {

    void renderZoomOverlay_JustZoom(PoseStack graphics);

}
