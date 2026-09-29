package de.keksuccino.justzoom.util.config.gui;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Adapts vanilla's scrolling option rows to the widget-based tab API on versions without ScrollableLayout.
 * The vanilla list owns dragging, keyboard traversal, and narration; this wrapper adds transparent
 * clipping and makes the whole list one tab child. Keep event and focus delegation together when porting.
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
        this.height = height;
        this.y = top;
        this.list.rowWidth = rowWidth;
        this.list.updateSize(width, height + top, top, top + height);
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
        if (this.focused != null && this.focused != focused) clearChildFocus(this.focused);
        this.focused = focused;
    }

    private static void clearChildFocus(GuiEventListener listener) {
        if (listener instanceof AbstractWidget widget && widget.isFocused()) widget.changeFocus(false);
        if (listener instanceof ContainerEventHandler container) {
            for (GuiEventListener child : container.children()) clearChildFocus(child);
            container.setFocused(null);
            container.setDragging(false);
        }
    }

    @Override
    public boolean changeFocus(boolean forward) {
        return ContainerEventHandler.super.changeFocus(forward);
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
    public void renderButton(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // 1.19 clips lists with opaque background masks. The preview is transparent, so use
        // a real scissor rectangle and restore any enclosing scissor instead of those masks.
        var window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        boolean wasEnabled = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        int[] previous = new int[4];
        org.lwjgl.opengl.GL11.glGetIntegerv(org.lwjgl.opengl.GL11.GL_SCISSOR_BOX, previous);
        int left = (int) Math.floor(this.x * scale);
        int bottom = (int) Math.floor(window.getHeight() - (this.y + this.height) * scale);
        int right = (int) Math.ceil((this.x + this.width) * scale);
        int top = (int) Math.ceil(window.getHeight() - this.y * scale);
        if (wasEnabled) {
            left = Math.max(left, previous[0]);
            bottom = Math.max(bottom, previous[1]);
            right = Math.min(right, previous[0] + previous[2]);
            top = Math.min(top, previous[1] + previous[3]);
        }
        com.mojang.blaze3d.systems.RenderSystem.enableScissor(left, bottom, Math.max(0, right - left), Math.max(0, top - bottom));
        try {
            this.list.render(graphics, mouseX, mouseY, partialTicks);
        } finally {
            if (wasEnabled) com.mojang.blaze3d.systems.RenderSystem.enableScissor(previous[0], previous[1], previous[2], previous[3]);
            else com.mojang.blaze3d.systems.RenderSystem.disableScissor();
        }
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        this.list.updateNarration(output);
    }

    private static final class ListWidget extends ContainerObjectSelectionList<Row> {

        private int rowWidth;

        private ListWidget(@NotNull Minecraft minecraft, int width, int height, int top, int rowWidth) {
            super(minecraft, width, height + top, top, top + height, 26);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
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
        public void render(@NotNull PoseStack graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            for (AbstractWidget widget : this.widgets) {
                widget.x = left;
                widget.y = top + (20 - widget.getHeight()) / 2;
                widget.render(graphics, mouseX, mouseY, partialTicks);
                left += widget.getWidth() + 5;
            }
        }

    }

}
