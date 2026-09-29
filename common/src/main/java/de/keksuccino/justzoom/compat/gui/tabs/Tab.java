package de.keksuccino.justzoom.compat.gui.tabs;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

/** The widget lifecycle used by vanilla tabs, backported for Minecraft 1.19.2. */
public interface Tab {

    Component getTabTitle();

    void visitChildren(@NotNull Consumer<AbstractWidget> consumer);

    void doLayout(@NotNull Area area);

    default void tick() {
    }

    record Area(int left, int top, int width, int height) {

        public int bottom() {
            return this.top + this.height;
        }

    }

}
