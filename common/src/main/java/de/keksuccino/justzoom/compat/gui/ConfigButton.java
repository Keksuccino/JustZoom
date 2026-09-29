package de.keksuccino.justzoom.compat.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/** Minimal modern button builder on the native 1.19 button, retaining its input and drawing behavior. */
public final class ConfigButton extends Button {

    @Nullable private ConfigTooltip tooltip;
    @Nullable private final Function<Supplier<MutableComponent>, MutableComponent> narration;

    private ConfigButton(Builder builder) {
        super(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress);
        this.tooltip = builder.tooltip;
        this.narration = builder.narration;
    }

    public static Builder builder(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    public void setTooltip(@Nullable ConfigTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Nullable
    public ConfigTooltip getTooltip() {
        return this.tooltip;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.narration == null ? super.createNarrationMessage() : this.narration.apply(super::createNarrationMessage);
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        super.updateNarration(output);
        if (this.tooltip != null) output.add(NarratedElementType.HINT, this.tooltip.message());
    }

    public static final class Builder {

        private final Component message;
        private final OnPress onPress;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        @Nullable private ConfigTooltip tooltip;
        @Nullable private Function<Supplier<MutableComponent>, MutableComponent> narration;

        private Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            return this.size(width, height);
        }

        public Builder tooltip(ConfigTooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(Function<Supplier<MutableComponent>, MutableComponent> narration) {
            this.narration = narration;
            return this;
        }

        public ConfigButton build() {
            return new ConfigButton(this);
        }

    }

}
