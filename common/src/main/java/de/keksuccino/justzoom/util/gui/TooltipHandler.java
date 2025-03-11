package de.keksuccino.justzoom.util.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TooltipHandler {

    private static final Map<String, Tooltip> TOOLTIPS = new HashMap<>();

    public static void putTooltip(@NotNull String id, @NotNull Tooltip tooltip) {
        TOOLTIPS.put(id, tooltip);
    }

    public static void clearAll() {
        TOOLTIPS.clear();
    }

    public static void tick() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null) {
            TOOLTIPS.forEach((id, tooltip) -> {
                if (tooltip.shouldRender.get()) {
                    PostRenderTasks.post((pose, mouseX, mouseY, partial) -> {
                        screen.renderTooltip(pose, tooltip.tooltip, mouseX, mouseY);
                    });
                }
            });
        }
    }

    public static class Tooltip {

        protected Supplier<Boolean> shouldRender;
        protected List<FormattedCharSequence> tooltip;

        @NotNull
        public static Tooltip widget(@NotNull AbstractWidget widget, @NotNull String localizationKey) {
            String s = I18n.get(localizationKey);
            List<String> lines = s.contains("\n") ? List.of(s.split("\n", -1)) : List.of(s);
            List<FormattedCharSequence> sequences = new ArrayList<>();
            lines.forEach(line -> {
                sequences.addAll(Minecraft.getInstance().font.split(FormattedText.composite(new TextComponent(line)), 150));
            });
            return new Tooltip(() -> (!widget.isFocused() && widget.isHoveredOrFocused()), sequences);
        }

        @NotNull
        public static Tooltip widget(@NotNull AbstractWidget widget, @NotNull Component... tooltip) {
            List<FormattedCharSequence> sequences = new ArrayList<>();
            for (Component c : tooltip) {
                sequences.add(c.getVisualOrderText());
            }
            return new Tooltip(() -> (!widget.isFocused() && widget.isHoveredOrFocused()), sequences);
        }

        @NotNull
        public static Tooltip widget(@NotNull AbstractWidget widget, @NotNull FormattedCharSequence... tooltip) {
            return new Tooltip(() -> (!widget.isFocused() && widget.isHoveredOrFocused()), List.of(tooltip));
        }

        @NotNull
        public static Tooltip generic(@NotNull Supplier<Boolean> shouldRender, @NotNull String localizationKey) {
            String s = I18n.get(localizationKey);
            List<String> lines = s.contains("\n") ? List.of(s.split("\n", -1)) : List.of(s);
            List<FormattedCharSequence> sequences = new ArrayList<>();
            lines.forEach(line -> {
                sequences.addAll(Minecraft.getInstance().font.split(FormattedText.composite(new TextComponent(line)), 150));
            });
            return new Tooltip(shouldRender, sequences);
        }

        @NotNull
        public static Tooltip generic(@NotNull Supplier<Boolean> shouldRender, @NotNull Component... tooltip) {
            List<FormattedCharSequence> sequences = new ArrayList<>();
            for (Component c : tooltip) {
                sequences.add(c.getVisualOrderText());
            }
            return new Tooltip(shouldRender, sequences);
        }

        @NotNull
        public static Tooltip generic(@NotNull Supplier<Boolean> shouldRender, @NotNull FormattedCharSequence... tooltip) {
            return new Tooltip(shouldRender, List.of(tooltip));
        }

        protected Tooltip(@NotNull Supplier<Boolean> shouldRender, @NotNull List<FormattedCharSequence> tooltip) {
            this.shouldRender = shouldRender;
            this.tooltip = tooltip;
        }

    }

}
