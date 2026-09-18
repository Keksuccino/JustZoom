package de.keksuccino.justzoom.mixin;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinTargetsTest {

    private static final List<String> INJECTORS = List.of("Lorg/spongepowered/asm/mixin/injection/Inject;", "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;", "Lcom/llamalad7/mixinextras/injector/v2/WrapWithCondition;");

    // Inspect bytecode without loading Minecraft or starting Mixin. Java compilation cannot
    // validate method names and descriptors stored in injection annotations.
    @TestFactory
    Stream<DynamicTest> commonInjectionTargetsExist() throws IOException {
        List<DynamicTest> tests = new ArrayList<>();
        try (var stream = getClass().getResourceAsStream("/justzoom.mixins.json")) {
            assertNotNull(stream);
            var config = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            String mixinPackage = config.get("package").getAsString();
            for (var entry : config.getAsJsonArray("client")) {
                ClassNode mixin = readClass((mixinPackage + "." + entry.getAsString()).replace('.', '/'));
                AnnotationNode annotation = annotations(mixin.visibleAnnotations, mixin.invisibleAnnotations).filter(value -> value.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
                List<Type> targets = annotationValue(annotation, "value");
                for (Type target : targets) {
                    ClassNode targetClass = readClass(target.getInternalName());
                    for (MethodNode handler : mixin.methods) {
                        annotations(handler.visibleAnnotations, handler.invisibleAnnotations).filter(value -> INJECTORS.contains(value.desc)).forEach(injector -> {
                            tests.add(DynamicTest.dynamicTest(entry.getAsString() + "." + handler.name, () -> verifyInjection(targetClass, injector)));
                        });
                    }
                }
            }
        }
        assertFalse(tests.isEmpty());
        return tests.stream();
    }

    private static void verifyInjection(ClassNode targetClass, AnnotationNode injector) {
        List<String> selectors = annotationValue(injector, "method");
        assertNotNull(selectors);
        Object atValue = annotationValue(injector, "at");
        List<?> injectionPoints = atValue instanceof List<?> list ? list : List.of(atValue);
        for (String selector : selectors) {
            List<MethodNode> methods = targetClass.methods.stream().filter(method -> selector.equals(method.name) || selector.equals(method.name + method.desc)).toList();
            assertFalse(methods.isEmpty(), () -> "Missing target " + targetClass.name + "." + selector);
            for (Object value : injectionPoints) {
                AnnotationNode at = (AnnotationNode) value;
                String kind = annotationValue(at, "value");
                String member = annotationValue(at, "target");
                if ("HEAD".equals(kind)) {
                    assertTrue(methods.stream().allMatch(method -> method.instructions.size() > 0));
                } else if ("RETURN".equals(kind)) {
                    assertTrue(methods.stream().anyMatch(method -> Stream.of(method.instructions.toArray()).anyMatch(instruction -> instruction.getOpcode() >= Opcodes.IRETURN && instruction.getOpcode() <= Opcodes.RETURN)));
                } else if ("INVOKE".equals(kind)) {
                    assertTrue(methods.stream().anyMatch(method -> Stream.of(method.instructions.toArray()).anyMatch(instruction -> instruction instanceof MethodInsnNode call && member.equals("L" + call.owner + ";" + call.name + call.desc))), () -> "Missing invocation " + member + " in " + targetClass.name + "." + selector);
                } else if ("FIELD".equals(kind)) {
                    assertTrue(methods.stream().anyMatch(method -> Stream.of(method.instructions.toArray()).anyMatch(instruction -> instruction instanceof FieldInsnNode field && member.equals("L" + field.owner + ";" + field.name + ":" + field.desc))), () -> "Missing field access " + member + " in " + targetClass.name + "." + selector);
                } else {
                    throw new AssertionError("Unsupported injection point: " + kind);
                }
            }
        }
    }

    private static ClassNode readClass(String name) throws IOException {
        try (var stream = MixinTargetsTest.class.getResourceAsStream("/" + name + ".class")) {
            assertNotNull(stream, () -> "Missing class " + name);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        }
    }

    private static Stream<AnnotationNode> annotations(List<AnnotationNode> visible, List<AnnotationNode> invisible) {
        return Stream.concat(visible == null ? Stream.empty() : visible.stream(), invisible == null ? Stream.empty() : invisible.stream());
    }

    @SuppressWarnings("unchecked")
    private static <T> T annotationValue(AnnotationNode annotation, String name) {
        if (annotation.values != null) {
            for (int i = 0; i < annotation.values.size(); i += 2) {
                if (name.equals(annotation.values.get(i))) {
                    return (T) annotation.values.get(i + 1);
                }
            }
        }
        return null;
    }

}
