package de.keksuccino.justzoom.util.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class PostRenderTasks {

    private static final List<RenderTask> TASKS = new ArrayList<>();

    public static void post(@NotNull RenderTask task) {
        TASKS.add(task);
    }

    public static List<RenderTask> getAllAndClear() {
        List<RenderTask> copy = new ArrayList<>(TASKS);
        TASKS.clear();
        return copy;
    }

    @FunctionalInterface
    public interface RenderTask {

        void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial);

    }

}
