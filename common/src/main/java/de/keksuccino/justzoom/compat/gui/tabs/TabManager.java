package de.keksuccino.justzoom.compat.gui.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/** Owns tab attachment, layout and removal, keeping inactive controls out of screen input dispatch. */
public final class TabManager {

    private final Consumer<AbstractWidget> addWidget;
    private final Consumer<AbstractWidget> removeWidget;
    private final Runnable playClickSound;
    @Nullable private Tab currentTab;
    @Nullable private Tab.Area tabArea;

    public TabManager(Consumer<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        this(addWidget, removeWidget, () -> Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)));
    }

    TabManager(Consumer<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget, Runnable playClickSound) {
        this.addWidget = Objects.requireNonNull(addWidget);
        this.removeWidget = Objects.requireNonNull(removeWidget);
        this.playClickSound = Objects.requireNonNull(playClickSound);
    }

    public void setTabArea(@NotNull Tab.Area area) {
        this.tabArea = Objects.requireNonNull(area);
        if (this.currentTab != null) this.currentTab.doLayout(area);
    }

    public void setCurrentTab(@NotNull Tab tab, boolean playSound) {
        Objects.requireNonNull(tab);
        if (tab == this.currentTab) return;
        if (this.currentTab != null) this.currentTab.visitChildren(this.removeWidget);
        this.currentTab = tab;
        tab.visitChildren(this.addWidget);
        if (this.tabArea != null) tab.doLayout(this.tabArea);
        if (playSound) this.playClickSound.run();
    }

    @Nullable
    public Tab getCurrentTab() {
        return this.currentTab;
    }

    public void tickCurrent() {
        if (this.currentTab != null) this.currentTab.tick();
    }

}
