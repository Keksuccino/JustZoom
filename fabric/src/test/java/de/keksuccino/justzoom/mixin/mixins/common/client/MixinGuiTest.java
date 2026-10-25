package de.keksuccino.justzoom.mixin.mixins.common.client;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MixinGuiTest {

    private static final String RENDER_SYSTEM = "com/mojang/blaze3d/systems/RenderSystem";

    // The overlay's blend state only shows up on the GPU, so check the production bytecode for the state setup around the vanilla call.
    @Test
    void hiddenHudSpyglassOverlayIsDrawnWithBlending() throws IOException {
        ClassNode node = new ClassNode();
        try (var stream = getClass().getResourceAsStream("/de/keksuccino/justzoom/mixin/mixins/common/client/MixinGui.class")) {
            assertNotNull(stream);
            new ClassReader(stream).accept(node, ClassReader.SKIP_FRAMES);
        }
        MethodNode method = node.methods.stream().filter(candidate -> candidate.name.equals("renderZoomOverlay_JustZoom")).findFirst().orElseThrow();

        List<String> calls = StreamSupport.stream(method.instructions.spliterator(), false)
                .filter(instruction -> instruction instanceof MethodInsnNode call && (call.owner.equals(RENDER_SYSTEM) || call.name.equals("renderSpyglassOverlay")))
                .map(instruction -> ((MethodInsnNode) instruction).name)
                .toList();

        assertEquals(List.of("enableBlend", "defaultBlendFunc", "renderSpyglassOverlay", "disableBlend"), calls);
    }

}
