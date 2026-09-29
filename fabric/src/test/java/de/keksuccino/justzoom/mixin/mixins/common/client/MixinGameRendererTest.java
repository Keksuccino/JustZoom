package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.ZoomMath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MixinGameRendererTest {

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
        ClassNode node = new ClassNode();
        try (var stream = getClass().getResourceAsStream("/de/keksuccino/justzoom/mixin/mixins/common/client/MixinGameRenderer.class")) {
            assertNotNull(stream);
            new ClassReader(stream).accept(node, ClassReader.SKIP_FRAMES);
        }
        // Substitute only the runtime input/cache boundary. The production injection handlers and
        // real ZoomMath implementation execute unchanged, without loading config or native windows.
        for (var method : node.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("de/keksuccino/justzoom/ZoomHandler")) {
                    call.owner = Type.getInternalName(FakeZoomHandler.class);
                }
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Class<?> type = new TestClassLoader().define(writer.toByteArray());
        this.mixin = type.getConstructor().newInstance();
        this.calculate = Arrays.stream(type.getDeclaredMethods()).filter(method -> method.getName().contains("getFov")).findFirst().orElseThrow();
        this.calculate.setAccessible(true);
        this.tick = type.getDeclaredMethod("after_tickFov_JustZoom", CallbackInfo.class);
        this.tick.setAccessible(true);
    }

    @Test
    void inactiveZoomPreservesVanillaFovAndRefreshesSensitivityCache() throws ReflectiveOperationException {
        var result = calculate(73.25F, true);
        assertFalse(result.isCancelled());
        assertEquals(73.25F, result.getReturnValue().floatValue());
        assertEquals(73.25F, FakeZoomHandler.normalFov);
        assertEquals(73.25F, FakeZoomHandler.modifiedFov);
        assertEquals(0.35F, FakeZoomHandler.partialTicks);
    }

    @Test
    void magnificationUsesFinishedVanillaFovAndUpdatesCache() throws ReflectiveOperationException {
        FakeZoomHandler.magnification = 8.0D;
        var result = calculate(47.0F, true);
        assertTrue(result.isCancelled());
        assertEquals(ZoomMath.calculateZoomedFov(47.0F, 8.0D), result.getReturnValue().floatValue());
        assertEquals(47.0F, FakeZoomHandler.normalFov);
        assertEquals(result.getReturnValue().floatValue(), FakeZoomHandler.modifiedFov);
    }

    @Test
    void maximumZoomReachesItsPositiveFloorAndReleaseRestoresVanilla() throws ReflectiveOperationException {
        FakeZoomHandler.magnification = ZoomMath.MAX_MAGNIFICATION;
        assertEquals(ZoomMath.MIN_FOV, calculate(70.0F, true).getReturnValue().floatValue());
        FakeZoomHandler.magnification = 1.0D;
        assertFalse(calculate(70.0F, true).isCancelled());
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
        calculate(47.0F, true);
        float cachedFov = FakeZoomHandler.modifiedFov;
        assertFalse(calculate(70.0F, false).isCancelled());
        assertEquals(47.0F, FakeZoomHandler.normalFov);
        assertEquals(cachedFov, FakeZoomHandler.modifiedFov);
    }

    private CallbackInfoReturnable<Double> calculate(float normalFov, boolean worldProjection) throws ReflectiveOperationException {
        CallbackInfoReturnable<Double> info = new CallbackInfoReturnable<>("getFov", true, (double) normalFov);
        this.calculate.invoke(this.mixin, null, 0.35F, worldProjection, info);
        return info;
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
