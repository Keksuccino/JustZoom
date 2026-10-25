package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import de.keksuccino.justzoom.ZoomMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MixinGameRendererTest {

    private ClassNode node;
    private Object mixin;
    private Method calculate;
    private Method tick;

    @BeforeEach
    void loadProductionMixinWithoutMinecraftRuntime() throws ReflectiveOperationException, IOException {
        FakeZoomHandler.magnification = 1.0D;
        FakeZoomHandler.ticks = 0;
        FakeZoomHandler.normalFov = Float.NaN;
        FakeZoomHandler.modifiedFov = Float.NaN;
        FakeZoomHandler.partialTicks = Float.NaN;
        this.node = new ClassNode();
        try (var stream = getClass().getResourceAsStream("/de/keksuccino/justzoom/mixin/mixins/common/client/MixinGameRenderer.class")) {
            assertNotNull(stream);
            new ClassReader(stream).accept(this.node, ClassReader.SKIP_FRAMES);
        }
        // Substitute only the runtime input/cache boundary. The production injection handlers and
        // real ZoomMath implementation execute unchanged, without loading config or native windows.
        for (var method : this.node.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("de/keksuccino/justzoom/ZoomHandler")) {
                    call.owner = Type.getInternalName(FakeZoomHandler.class);
                }
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        this.node.accept(writer);
        Class<?> type = new TestClassLoader().define(writer.toByteArray());
        this.mixin = type.getConstructor().newInstance();
        this.calculate = Arrays.stream(type.getDeclaredMethods()).filter(method -> method.getName().contains("getFov")).findFirst().orElseThrow();
        this.calculate.setAccessible(true);
        this.tick = type.getDeclaredMethod("after_tickFov_JustZoom", CallbackInfo.class);
        this.tick.setAccessible(true);
    }

    @Test
    void inactiveZoomReturnsFinishedFovUnchangedAndRefreshesSensitivityCache() throws ReflectiveOperationException {
        FakeOriginal original = new FakeOriginal(73.25D);
        assertEquals(73.25D, calculate(original, true));
        assertEquals(1, original.calls);
        assertArrayEquals(new Object[]{null, 0.35F, true}, original.arguments);
        assertEquals(73.25F, FakeZoomHandler.normalFov);
        assertEquals(73.25F, FakeZoomHandler.modifiedFov);
        assertEquals(0.35F, FakeZoomHandler.partialTicks);
    }

    @Test
    void magnificationZoomsFovFinishedByOtherModsAndUpdatesCache() throws ReflectiveOperationException {
        // The wrapped method includes other mods' injectors, e.g. a RETURN callback that cancels with a widened FOV.
        FakeZoomHandler.magnification = 8.0D;
        double result = calculate(new FakeOriginal(84.0D), true);
        assertEquals(ZoomMath.calculateZoomedFov(84.0F, 8.0D), (float) result);
        assertEquals(84.0F, FakeZoomHandler.normalFov);
        assertEquals((float) result, FakeZoomHandler.modifiedFov);
    }

    @Test
    void maximumZoomReachesItsPositiveFloorAndReleaseRestoresFinishedFov() throws ReflectiveOperationException {
        FakeZoomHandler.magnification = ZoomMath.MAX_MAGNIFICATION;
        assertEquals(ZoomMath.MIN_FOV, (float) calculate(new FakeOriginal(70.0D), true));
        FakeZoomHandler.magnification = 1.0D;
        assertEquals(70.0D, calculate(new FakeOriginal(70.0D), true));
        assertEquals(70.0F, FakeZoomHandler.modifiedFov);
    }

    @Test
    void completedVanillaFovTickAdvancesZoomExactlyOnce() throws ReflectiveOperationException {
        this.tick.invoke(this.mixin, new CallbackInfo("tickFov", false));
        assertEquals(1, FakeZoomHandler.ticks);
    }

    @Test
    void handProjectionNeverZoomsOrOverwritesWorldSensitivityCache() throws ReflectiveOperationException {
        FakeZoomHandler.magnification = 8.0D;
        calculate(new FakeOriginal(47.0D), true);
        float cachedFov = FakeZoomHandler.modifiedFov;
        FakeOriginal original = new FakeOriginal(70.0D);
        assertEquals(70.0D, calculate(original, false));
        assertEquals(1, original.calls);
        assertEquals(47.0F, FakeZoomHandler.normalFov);
        assertEquals(cachedFov, FakeZoomHandler.modifiedFov);
    }

    @Test
    void fovHookWrapsWholeMethodSoCancellingReturnCallbacksCannotSkipIt() {
        // Once one RETURN callback cancels, Mixin skips all RETURN callbacks applied after it, and the order between mods is not stable.
        MethodNode handler = this.node.methods.stream().filter(method -> method.name.contains("getFov")).findFirst().orElseThrow();
        assertTrue(annotations(handler).anyMatch(annotation -> annotation.desc.equals(Type.getDescriptor(WrapMethod.class)) && List.of("getFov").equals(annotationValue(annotation, "method"))));
        assertFalse(this.node.methods.stream().flatMap(MixinGameRendererTest::annotations).anyMatch(annotation -> annotation.desc.equals(Type.getDescriptor(Inject.class)) && List.of("getFov").equals(annotationValue(annotation, "method"))));
    }

    private double calculate(FakeOriginal original, boolean worldProjection) throws ReflectiveOperationException {
        return (double) this.calculate.invoke(this.mixin, null, 0.35F, worldProjection, original);
    }

    private static Stream<AnnotationNode> annotations(MethodNode method) {
        return Stream.concat(method.visibleAnnotations == null ? Stream.empty() : method.visibleAnnotations.stream(), method.invisibleAnnotations == null ? Stream.empty() : method.invisibleAnnotations.stream());
    }

    private static Object annotationValue(AnnotationNode annotation, String name) {
        if (annotation.values == null) return null;
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (name.equals(annotation.values.get(i))) return annotation.values.get(i + 1);
        }
        return null;
    }

    private static final class FakeOriginal implements Operation<Double> {

        private final double fov;
        private int calls;
        private Object[] arguments;

        private FakeOriginal(double fov) {
            this.fov = fov;
        }

        @Override
        public Double call(Object... args) {
            this.calls++;
            this.arguments = args;
            return this.fov;
        }

    }

    public static final class FakeZoomHandler {

        public static double magnification;
        public static int ticks;
        public static float normalFov;
        public static float modifiedFov;
        public static float partialTicks;

        public static double getRenderedMagnification(float partial, float normal) {
            partialTicks = partial;
            return magnification;
        }

        public static void updateRenderedFov(float normal, float modified) {
            normalFov = normal;
            modifiedFov = modified;
        }

        public static void onCameraTick() {
            ticks++;
        }

    }

    private static final class TestClassLoader extends ClassLoader {

        private TestClassLoader() {
            super(MixinGameRendererTest.class.getClassLoader());
        }

        private Class<?> define(byte[] bytecode) {
            return defineClass(null, bytecode, 0, bytecode.length);
        }

    }

}
