package de.keksuccino.justzoom.util.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScreenRenderUtil {

    private static final List<ScreenRenderContext> CONTEXTS = Collections.synchronizedList(new ArrayList<>());

    public static void renderAfterScreen(@NotNull ScreenRenderContext context) {
        CONTEXTS.add(context);
    }

    public static List<ScreenRenderContext> getContextsAndClearList() {
        List<ScreenRenderContext> contexts = new ArrayList<>(CONTEXTS);
        CONTEXTS.clear();
        return contexts;
    }

    @FunctionalInterface
    public interface ScreenRenderContext {

        void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial);

    }

}
