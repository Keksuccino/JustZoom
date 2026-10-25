package de.keksuccino.justzoom;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * A {@link TutorialToast} that wraps its text into as many lines as needed and hides itself after a display time,
 * since {@link TutorialToast} draws unwrapped single lines and has no display time in this Minecraft version.
 * It extends {@link TutorialToast} instead of only implementing {@link Toast}, so toast filters of other mods (like Sodium Extra) still treat it as a tutorial toast.
 */
public class TimedTutorialToast extends TutorialToast {

    private static final int TITLE_COLOR = 0x500050;
    private static final int TEXT_COLOR = 0xFF000000;
    private static final int WIDTH = 160;
    private static final int BACKGROUND_HEIGHT = 32;
    private static final int BACKGROUND_BORDER = 3;
    private static final int PADDING_TOP = 7;
    private static final int PADDING_BOTTOM = 3;
    private static final int LINE_SPACING = 11;
    private static final int MIN_CONTENT_LINES = 2;
    private static final int TEXT_LEFT = 30;
    private static final int TEXT_WIDTH = 126;
    // Position of the tutorial toast background inside Toast.TEXTURE.
    private static final int BACKGROUND_V = 96;

    private final TutorialToast.Icons icon;
    private final List<FormattedCharSequence> lines;
    private final int timeToDisplayMs;
    private boolean hidden;

    public TimedTutorialToast(@NotNull Font font, @NotNull TutorialToast.Icons icon, @NotNull Component title, int timeToDisplayMs) {
        this(icon, title, font.split(title.copy().withStyle(style -> style.withColor(TITLE_COLOR)), TEXT_WIDTH), timeToDisplayMs);
    }

    TimedTutorialToast(@NotNull TutorialToast.Icons icon, @NotNull Component title, @NotNull List<FormattedCharSequence> lines, int timeToDisplayMs) {
        super(icon, title, null, false);
        this.icon = icon;
        this.lines = List.copyOf(lines);
        this.timeToDisplayMs = timeToDisplayMs;
    }

    @NotNull
    @Override
    public Toast.Visibility render(@NotNull GuiGraphics graphics, @NotNull ToastComponent toastComponent, long fullyVisibleForMs) {
        this.renderBackground(graphics);
        this.icon.render(graphics, 6, 6);
        Font font = toastComponent.getMinecraft().font;
        int textTop = PADDING_TOP + (this.contentHeight() - this.lines.size() * LINE_SPACING) / 2;
        for (int i = 0; i < this.lines.size(); i++) {
            graphics.drawString(font, this.lines.get(i), TEXT_LEFT, textTop + i * LINE_SPACING, TEXT_COLOR, false);
        }
        return this.getVisibility(fullyVisibleForMs);
    }

    @Override
    public int height() {
        return PADDING_TOP + this.contentHeight() + PADDING_BOTTOM;
    }

    @Override
    public void hide() {
        super.hide();
        this.hidden = true;
    }

    @NotNull
    Toast.Visibility getVisibility(long fullyVisibleForMs) {
        if (fullyVisibleForMs > this.timeToDisplayMs) this.hide();
        return this.hidden ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    private int contentHeight() {
        return Math.max(this.lines.size(), MIN_CONTENT_LINES) * LINE_SPACING;
    }

    /**
     * The background texture has a fixed height in this Minecraft version,
     * so taller toasts repeat its inner rows between its top and bottom border rows instead of stretching it.
     */
    private void renderBackground(@NotNull GuiGraphics graphics) {
        int height = this.height();
        int bottomBorderY = height - BACKGROUND_BORDER;
        int y = Math.min(bottomBorderY, BACKGROUND_HEIGHT - BACKGROUND_BORDER);
        graphics.blit(TEXTURE, 0, 0, 0, BACKGROUND_V, WIDTH, y);
        while (y < bottomBorderY) {
            int rows = Math.min(BACKGROUND_HEIGHT - 2 * BACKGROUND_BORDER, bottomBorderY - y);
            graphics.blit(TEXTURE, 0, y, 0, BACKGROUND_V + BACKGROUND_BORDER, WIDTH, rows);
            y += rows;
        }
        graphics.blit(TEXTURE, 0, bottomBorderY, 0, BACKGROUND_V + BACKGROUND_HEIGHT - BACKGROUND_BORDER, WIDTH, BACKGROUND_BORDER);
    }

}
