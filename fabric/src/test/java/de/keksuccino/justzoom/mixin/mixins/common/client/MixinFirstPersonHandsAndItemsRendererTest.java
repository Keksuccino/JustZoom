package de.keksuccino.justzoom.mixin.mixins.common.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinFirstPersonHandsAndItemsRendererTest {

    private Object mixin;
    private Method handler;
    private Field hideArms;

    @BeforeEach
    void loadIsolatedMixin() throws ReflectiveOperationException, IOException {
        // Keep the production mixin bytecode intact, but supply a tiny ZoomHandler fake
        // in its own class loader so these tests never initialize Minecraft or user config.
        TestClassLoader loader = new TestClassLoader();
        ClassWriter fake = new ClassWriter(0);
        String zoomHandler = "de/keksuccino/justzoom/ZoomHandler";
        fake.visit(Opcodes.V25, Opcodes.ACC_PUBLIC, zoomHandler, null, "java/lang/Object", null);
        fake.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "hideArms", "Z", null, null).visitEnd();
        var predicate = fake.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "shouldHideArmsWhenZooming", "()Z", null, null);
        predicate.visitCode();
        predicate.visitFieldInsn(Opcodes.GETSTATIC, zoomHandler, "hideArms", "Z");
        predicate.visitInsn(Opcodes.IRETURN);
        predicate.visitMaxs(1, 0);
        predicate.visitEnd();
        fake.visitEnd();
        this.hideArms = loader.define(fake.toByteArray()).getField("hideArms");

        String mixinName = "de/keksuccino/justzoom/mixin/mixins/common/client/MixinFirstPersonHandsAndItemsRenderer";
        try (var stream = getClass().getResourceAsStream("/" + mixinName + ".class")) {
            assertNotNull(stream);
            Class<?> mixinClass = loader.define(stream.readAllBytes());
            this.mixin = mixinClass.getConstructor().newInstance();
            this.handler = mixinClass.getDeclaredMethod("cancel_submitHandsWithItems_JustZoom", CallbackInfo.class);
            this.handler.setAccessible(true);
        }
    }

    @Test
    void cancelsBeforeAnyHandSubmissionWhenHidden() throws ReflectiveOperationException {
        Inject injection = this.handler.getAnnotation(Inject.class);
        assertNotNull(injection);
        assertTrue(injection.cancellable());
        assertEquals("HEAD", injection.at()[0].value());
        this.hideArms.setBoolean(null, true);
        assertTrue(invokeHandler().isCancelled());
    }

    @Test
    void leavesNormalHandRenderingEnabled() throws ReflectiveOperationException {
        assertFalse(invokeHandler().isCancelled());
    }

    @Test
    void reevaluatesEachPassAndRestoresRenderingAfterZoom() throws ReflectiveOperationException {
        assertFalse(invokeHandler().isCancelled());
        this.hideArms.setBoolean(null, true);
        assertTrue(invokeHandler().isCancelled());
        assertTrue(invokeHandler().isCancelled());
        this.hideArms.setBoolean(null, false);
        assertFalse(invokeHandler().isCancelled());
        this.hideArms.setBoolean(null, true);
        assertTrue(invokeHandler().isCancelled());
    }

    private CallbackInfo invokeHandler() throws ReflectiveOperationException {
        CallbackInfo info = new CallbackInfo("submitHandsWithItems", true);
        this.handler.invoke(this.mixin, info);
        return info;
    }

    private static final class TestClassLoader extends ClassLoader {

        private TestClassLoader() {
            super(MixinFirstPersonHandsAndItemsRendererTest.class.getClassLoader());
        }

        private Class<?> define(byte[] bytecode) {
            return defineClass(null, bytecode, 0, bytecode.length);
        }

    }

}
