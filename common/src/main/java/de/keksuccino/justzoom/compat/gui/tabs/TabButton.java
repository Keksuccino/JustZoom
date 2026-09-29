package de.keksuccino.justzoom.compat.gui.tabs;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.justzoom.compat.gui.WidgetRenderUtils;
import de.keksuccino.justzoom.compat.gui.TabRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.NotNull;

/** Modern menu tab appearance and selection semantics, rendered through the 1.19 drawing API. */
final class TabButton extends AbstractWidget {

    private final TabManager manager;
    private final Tab tab;

    TabButton(TabManager manager, Tab tab) {
        super(0, 0, 0, 24, tab.getTabTitle());
        this.manager = manager;
        this.tab = tab;
    }

    Tab tab() {
        return this.tab;
    }

    float opacity() {
        return this.alpha;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.manager.setCurrentTab(this.tab, true);
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        // The manager owns the sound so selecting the already active tab remains silent.
    }

    @Override
    public void renderButton(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        // Legacy Font treats a zero alpha byte as opaque, so do not submit fully faded labels.
        if (this.alpha <= 0.0F) return;
        boolean selected = this.manager.getCurrentTab() == this.tab;
        TabRenderUtils.drawFrame(pose, this.x, this.y, this.width, this.height, selected, this.isHoveredOrFocused(), this.alpha);
        var font = Minecraft.getInstance().font;
        var label = font.plainSubstrByWidth(this.getMessage().getString(), Math.max(0, this.width - 8));
        int color = WidgetRenderUtils.multiplyAlpha(this.active ? 0xFFFFFFFF : 0xFFA0A0A0, this.alpha);
        drawCenteredString(pose, font, label, this.x + this.width / 2, this.y + (this.height - 8) / 2 + (selected ? 0 : 3), color);
        if (selected) {
            int length = Math.min(font.width(label), this.width - 4);
            int left = this.x + (this.width - length) / 2;
            fill(pose, left, this.y + this.height - 2, left + length, this.y + this.height - 1, color);
        }
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

}
