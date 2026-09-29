package de.keksuccino.justzoom.compat.gui.tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.justzoom.compat.gui.TabRenderUtils;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Backports vanilla tab grouping, arrow navigation, Ctrl+Tab/Ctrl+number shortcuts and narration. */
public final class TabNavigationBar extends AbstractContainerEventHandler implements Widget, NarratableEntry {

    private final TabManager manager;
    private final List<Tab> tabs;
    private final List<TabButton> buttons;
    private final int width;
    private final Component narrationUsage;
    private final java.util.function.BiFunction<Integer, Integer, Component> narrationPosition;

    private TabNavigationBar(TabManager manager, int width, List<Tab> tabs, Component narrationUsage, java.util.function.BiFunction<Integer, Integer, Component> narrationPosition) {
        if (tabs.isEmpty()) throw new IllegalArgumentException("At least one tab is required.");
        this.manager = manager;
        this.narrationUsage = narrationUsage;
        this.narrationPosition = narrationPosition;
        this.width = width;
        this.tabs = List.copyOf(tabs);
        this.buttons = tabs.stream().map(tab -> new TabButton(manager, tab)).toList();
    }

    public static Builder builder(TabManager manager, int width) {
        return new Builder(manager, width);
    }

    public void arrangeElements() {
        int available = Math.max(4 * this.tabs.size(), Math.min(400, this.width) - 28);
        int tabWidth = Math.max(4, (available / this.tabs.size() + 1) / 2 * 2);
        int x = (this.width - tabWidth * this.tabs.size()) / 2;
        for (TabButton button : this.buttons) {
            button.x = x;
            button.y = 0;
            button.setWidth(tabWidth);
            x += tabWidth;
        }
    }

    public void selectTab(int index, boolean playSound) {
        this.manager.setCurrentTab(this.tabs.get(index), playSound);
        if (this.getFocused() != null) this.setFocused(this.buttons.get(index));
    }

    public boolean keyPressed(int keyCode) {
        int index = shortcutTabIndex(keyCode, Screen.hasControlDown(), Screen.hasShiftDown(), this.tabs.indexOf(this.manager.getCurrentTab()), this.tabs.size());
        if (index < 0) return false;
        this.selectTab(index, true);
        return true;
    }

    static int shortcutTabIndex(int keyCode, boolean control, boolean shift, int current, int count) {
        if (!control || count <= 0) return -1;
        if (keyCode >= 49 && keyCode <= 57) return Math.min(keyCode - 49, count - 1);
        if (keyCode == 258 && current >= 0) return Math.floorMod(current + (shift ? -1 : 1), count);
        return -1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keyPressed(keyCode)) return true;
        if (this.getFocused() != null && (keyCode == 263 || keyCode == 262)) {
            int current = this.tabs.indexOf(this.manager.getCurrentTab());
            int next = Math.max(0, Math.min(this.tabs.size() - 1, current + (keyCode == 263 ? -1 : 1)));
            this.selectTab(next, true);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean changeFocus(boolean forward) {
        if (this.getFocused() != null) {
            this.setFocused(null);
            return false;
        }
        int current = this.tabs.indexOf(this.manager.getCurrentTab());
        this.setFocused(this.buttons.get(Math.max(0, current)));
        return true;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (this.getFocused() instanceof TabButton previous) previous.setFocused(false);
        super.setFocused(focused);
        if (focused instanceof TabButton button) {
            button.setFocused(true);
            this.manager.setCurrentTab(button.tab(), true);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.buttons;
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        float alpha = this.buttons.get(0).opacity();
        TabRenderUtils.drawSeparator(pose, 0, this.buttons.get(0).x, 22, alpha);
        TabButton last = this.buttons.get(this.buttons.size() - 1);
        TabRenderUtils.drawSeparator(pose, last.x + last.getWidth(), this.width, 22, alpha);
        for (TabButton button : this.buttons) button.render(pose, mouseX, mouseY, partialTicks);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return this.buttons.stream().map(TabButton::narrationPriority).max(Comparator.naturalOrder()).orElse(NarrationPriority.NONE);
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        TabButton current = this.buttons.stream().filter(button -> button.narrationPriority() == NarrationPriority.HOVERED).findFirst().orElse(this.buttons.get(Math.max(0, this.tabs.indexOf(this.manager.getCurrentTab()))));
        current.updateNarration(output);
        output.add(NarratedElementType.POSITION, this.narrationPosition.apply(this.buttons.indexOf(current) + 1, this.buttons.size()));
        if (this.getFocused() != null) output.add(NarratedElementType.USAGE, this.narrationUsage);
    }

    public static final class Builder {

        private final TabManager manager;
        private final int width;
        private final List<Tab> tabs = new ArrayList<>();
        private Component narrationUsage = Component.translatable("narration.component_list.usage");
        private java.util.function.BiFunction<Integer, Integer, Component> narrationPosition = (index, count) -> Component.translatable("narrator.position.screen", index, count);

        private Builder(TabManager manager, int width) {
            this.manager = manager;
            this.width = width;
        }

        public Builder addTabs(Tab... tabs) {
            this.tabs.addAll(Arrays.asList(tabs));
            return this;
        }

        public Builder narration(Component usage, java.util.function.BiFunction<Integer, Integer, Component> position) {
            this.narrationUsage = java.util.Objects.requireNonNull(usage);
            this.narrationPosition = java.util.Objects.requireNonNull(position);
            return this;
        }

        public TabNavigationBar build() {
            return new TabNavigationBar(this.manager, this.width, this.tabs, this.narrationUsage, this.narrationPosition);
        }

    }

}
