package de.keksuccino.justzoom.compat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

/** Deferred tooltips are drawn after scrolling-list clipping has ended. */
public record ConfigTooltip(@NotNull Component message) {

    public ConfigTooltip {
        Objects.requireNonNull(message);
    }

    public static ConfigTooltip create(Component message) {
        return new ConfigTooltip(message);
    }

    public void render(Screen screen, PoseStack pose, int mouseX, int mouseY) {
        screen.renderTooltip(pose, Minecraft.getInstance().font.split(this.message, Math.max(40, Math.min(250, screen.width - 20))), mouseX, mouseY);
    }

}
