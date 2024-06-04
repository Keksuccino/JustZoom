package de.keksuccino.justzoom.util.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class Tooltips {

    public static Button.OnTooltip create(@NotNull Component text, @NotNull Screen screen) {

        return new Button.OnTooltip() {
            @Override
            public void onTooltip(@NotNull Button button, @NotNull PoseStack pose1, int i1, int i2) {
                ScreenRenderUtil.renderAfterScreen((pose, mouseX, mouseY, partial) -> screen.renderTooltip(pose, Minecraft.getInstance().font.split(text, Math.max(screen.width / 2 - 43, 170)), i1, i2));
            }
            @Override
            public void narrateTooltip(@NotNull Consumer<Component> componentConsumer) {
                componentConsumer.accept(text);
            }
        };

    }

}
