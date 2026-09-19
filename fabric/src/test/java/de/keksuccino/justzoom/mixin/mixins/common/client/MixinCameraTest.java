package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MixinCameraTest {

    private Object mixin;
    private Method calculate;

    @BeforeEach
    void loadIsolatedMixin() throws ReflectiveOperationException, IOException {
        FakeZoomHandler.zooming = false;
        FakeZoomHandler.smooth = true;
        FakeZoomHandler.zoomModifier = 0.025F;
        FakeJustZoom.options = new FakeOptions();
        // Execute the production handlers with in-memory options and input state. Only
        // dependency types are substituted, keeping Minecraft and user config uninitialized.
        Map<String, String> replacements = Map.of(
                "de/keksuccino/justzoom/ZoomHandler", Type.getInternalName(FakeZoomHandler.class),
                "de/keksuccino/justzoom/JustZoom", Type.getInternalName(FakeJustZoom.class),
                "de/keksuccino/justzoom/Options", Type.getInternalName(FakeOptions.class),
                "de/keksuccino/justzoom/util/AbstractOptions$Option", Type.getInternalName(FakeOption.class));
        ClassNode node = new ClassNode();
        try (var stream = getClass().getResourceAsStream("/de/keksuccino/justzoom/mixin/mixins/common/client/MixinCamera.class")) {
            assertNotNull(stream);
            new ClassReader(stream).accept(node, ClassReader.SKIP_FRAMES);
        }
        for (var method : node.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call) {
                    call.owner = replacements.getOrDefault(call.owner, call.owner);
                    for (var entry : replacements.entrySet()) {
                        call.desc = call.desc.replace("L" + entry.getKey() + ";", "L" + entry.getValue() + ";");
                    }
                } else if (instruction instanceof FieldInsnNode field) {
                    field.owner = replacements.getOrDefault(field.owner, field.owner);
                    for (var entry : replacements.entrySet()) {
                        field.desc = field.desc.replace("L" + entry.getKey() + ";", "L" + entry.getValue() + ";");
                    }
                }
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Class<?> type = new TestClassLoader().define(writer.toByteArray());
        this.mixin = type.getConstructor().newInstance();
        this.calculate = type.getDeclaredMethod("return_calculateFov_JustZoom", float.class, CallbackInfoReturnable.class);
        this.calculate.setAccessible(true);
    }

    @Test
    void smoothZoomReachesBeyondSpyglassLimit() throws ReflectiveOperationException {
        FakeZoomHandler.zooming = true;
        float current = 1.0F;
        for (int tick = 0; tick < 40; tick++) {
            current = tick(current, 0.025F);
        }
        assertEquals(0.025F, current, 0.000001F);
    }

    @Test
    void scrollingOutBelowSpyglassLimitStillInterpolates() throws ReflectiveOperationException {
        FakeZoomHandler.zooming = true;
        FakeZoomHandler.zoomModifier = 0.05F;
        assertEquals(0.03F, tick(0.01F, 0.05F), 0.000001F);
    }

    @Test
    void extremeSmoothZoomStaysPositiveAndKeepsUpperLimit() throws ReflectiveOperationException {
        FakeZoomHandler.zooming = true;
        assertEquals(0.0000000001F, clamp(-1.0F));
        assertEquals(0.0000000001F, clamp(0.0F));
        assertEquals(0.0000000001F, clamp(0.0000000001F));
        assertEquals(1.5F, clamp(2.0F));
    }

    @Test
    void inactiveAndInstantZoomKeepVanillaLimits() throws ReflectiveOperationException {
        assertEquals(0.1F, clamp(0.025F));
        assertEquals(1.5F, clamp(2.0F));
        FakeZoomHandler.zooming = true;
        FakeZoomHandler.smooth = false;
        assertEquals(0.1F, clamp(0.025F));
        assertEquals(1.5F, clamp(2.0F));
    }

    @Test
    void releaseRestoresNormalFovSmoothly() throws ReflectiveOperationException {
        FakeZoomHandler.zooming = true;
        float current = tick(0.025F, 0.025F);
        FakeZoomHandler.zooming = false;
        current = tick(current, 1.0F);
        assertEquals(0.5125F, current, 0.000001F);
        for (int tick = 0; tick < 30; tick++) {
            current = tick(current, 1.0F);
        }
        assertEquals(1.0F, current, 0.000001F);
    }

    @Test
    void smoothZoomKeepsScrollFactorUntilReleased() throws ReflectiveOperationException {
        FakeZoomHandler.zooming = true;
        FakeJustZoom.options.resetZoomFactorOnStopZooming.value = true;
        for (int frame = 0; frame < 3; frame++) {
            assertFalse(calculate(1.75F).isCancelled());
            assertEquals(0.025F, FakeZoomHandler.zoomModifier);
        }
        FakeZoomHandler.zooming = false;
        assertFalse(calculate(70.0F).isCancelled());
        assertEquals(0.25F, FakeZoomHandler.zoomModifier);
        FakeZoomHandler.zooming = true;
        calculate(17.5F);
        assertEquals(0.25F, FakeZoomHandler.zoomModifier);
    }

    @Test
    void releasePreservesScrollFactorWhenResetDisabled() throws ReflectiveOperationException {
        calculate(70.0F);
        assertEquals(0.025F, FakeZoomHandler.zoomModifier);
    }

    @Test
    void instantZoomStillScalesAndCachesFov() throws ReflectiveOperationException {
        FakeZoomHandler.zooming = true;
        FakeZoomHandler.smooth = false;
        FakeJustZoom.options.resetZoomFactorOnStopZooming.value = true;
        assertEquals(1.75F, calculate(70.0F).getReturnValue());
        assertEquals(70.0F, FakeZoomHandler.cachedNormalFov);
        assertEquals(1.75F, FakeZoomHandler.cachedModifiedFov);
        assertEquals(0.025F, FakeZoomHandler.zoomModifier);
        assertEquals(1.0F, calculate(1.0F).getReturnValue());
        FakeZoomHandler.zooming = false;
        calculate(70.0F);
        assertEquals(0.25F, FakeZoomHandler.zoomModifier);
    }

    private float tick(float current, float target) throws ReflectiveOperationException {
        return clamp(current + (target - current) * 0.5F);
    }

    private float clamp(float value) throws ReflectiveOperationException {
        int[] calls = {0};
        Operation<Float> original = arguments -> {
            calls[0]++;
            return Math.clamp((float) arguments[0], (float) arguments[1], (float) arguments[2]);
        };
        Method handler = this.mixin.getClass().getDeclaredMethod("wrap_clamp_in_tickFov_JustZoom", float.class, float.class, float.class, Operation.class);
        handler.setAccessible(true);
        float result = (float) handler.invoke(this.mixin, value, 0.1F, 1.5F, original);
        assertEquals(1, calls[0]);
        return result;
    }

    private CallbackInfoReturnable<Float> calculate(float fov) throws ReflectiveOperationException {
        CallbackInfoReturnable<Float> info = new CallbackInfoReturnable<>("calculateFov", true, fov);
        this.calculate.invoke(this.mixin, 0.5F, info);
        return info;
    }

    public static class FakeZoomHandler {

        public static boolean zooming;
        public static boolean smooth;
        public static float zoomModifier;
        public static float cachedNormalFov;
        public static float cachedModifiedFov;

        public static boolean isZooming() {
            return zooming;
        }

        public static boolean shouldZoomInOutSmooth() {
            return smooth;
        }

        public static float getFovModifier() {
            return zoomModifier;
        }

    }

    public static class FakeJustZoom {

        public static FakeOptions options;

        public static FakeOptions getOptions() {
            return options;
        }

    }

    public static class FakeOptions {

        public final FakeOption<Boolean> resetZoomFactorOnStopZooming = new FakeOption<>(false);
        public final FakeOption<Float> baseZoomFactor = new FakeOption<>(0.25F);

    }

    public static class FakeOption<T> {

        public T value;

        public FakeOption(T value) {
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

    }

    private static final class TestClassLoader extends ClassLoader {

        private TestClassLoader() {
            super(MixinCameraTest.class.getClassLoader());
        }

        private Class<?> define(byte[] bytecode) {
            return defineClass(null, bytecode, 0, bytecode.length);
        }

    }

}
