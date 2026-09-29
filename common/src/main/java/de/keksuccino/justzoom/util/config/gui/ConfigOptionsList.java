package de.keksuccino.justzoom.util.config.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Adapts vanilla's scrolling option rows to the widget-based tab API on versions without ScrollableLayout.
 * The vanilla list owns clipping, dragging, keyboard traversal, and narration; this wrapper only makes
 * the whole list one tab child. Keep its event and focus delegation together when porting versions.
 */
final class ConfigOptionsList extends AbstractWidget implements ContainerEventHandler {

    private final ListWidget list;
    @Nullable
    private GuiEventListener focused;
    private boolean dragging;

    ConfigOptionsList(@NotNull Minecraft minecraft, int width, int height, int top, int rowWidth) {
        super(0, top, width, height, CommonComponents.EMPTY);
        this.list = new ListWidget(minecraft, width, height, top, rowWidth);
    }

    void setRows(@NotNull List<Row> rows) {
        // TabManager visits children on removal and reattachment. Preserve each tab's scroll
        // position when its rows have not changed instead of rebuilding the native list.
        if (this.list.children().equals(rows)) return;
        this.list.replaceRows(rows);
        this.setFocused(null);
        this.setDragging(false);
    }

    void setBounds(int width, int height, int top, int rowWidth) {
        this.setWidth(width);
        this.setHeight(height);
        this.setY(top);
        this.list.rowWidth = rowWidth;
        this.list.updateSizeAndPosition(width, height, top);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(this.list);
    }

    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (this.focused != null) this.focused.setFocused(false);
        this.focused = focused;
        if (focused != null) focused.setFocused(true);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
        return ContainerEventHandler.super.nextFocusPath(event);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean isFocused() {
        return ContainerEventHandler.super.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        ContainerEventHandler.super.setFocused(focused);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.list.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        this.list.updateNarration(output);
    }

    private static final class ListWidget extends ContainerObjectSelectionList<Row> {

        private int rowWidth;

        private ListWidget(@NotNull Minecraft minecraft, int width, int height, int top, int rowWidth) {
            super(minecraft, width, height, top, 26);
            this.rowWidth = rowWidth;
        }

        private void replaceRows(@NotNull List<Row> rows) {
            this.setFocused(null);
            this.setDragging(false);
            this.replaceEntries(rows);
            this.setScrollAmount(0.0D);
        }

        @Override
        public int getRowWidth() {
            return this.rowWidth;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowRight() + 4;
        }

        @Override
        protected void renderListBackground(@NotNull GuiGraphics graphics) {
            // The screen owns its background, including the transparent world preview.
        }

        @Override
        protected void renderListSeparators(@NotNull GuiGraphics graphics) {
        }

    }

    static final class Row extends ContainerObjectSelectionList.Entry<Row> {

        private final List<AbstractWidget> widgets;

        Row(@NotNull List<AbstractWidget> widgets) {
            this.widgets = List.copyOf(widgets);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.widgets;
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            for (AbstractWidget widget : this.widgets) {
                widget.setX(left);
                widget.setY(top + (20 - widget.getHeight()) / 2);
                widget.render(graphics, mouseX, mouseY, partialTicks);
                left += widget.getWidth() + 5;
            }
        }

    }

}
