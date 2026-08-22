package de.keksuccino.justzoom.mixin.mixins.common.client;

import net.minecraft.client.renderer.ItemInHandRenderer;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.Inject;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinItemInHandRendererTargetTest {

    @Test
    void hideHandMixinTargetsThePerHandSubmissionUsedByVanillaAndIris() {
        assertDoesNotThrow(() -> Arrays.stream(ItemInHandRenderer.class.getDeclaredMethods()).filter(method -> method.getName().equals("submitArmWithItem")).findFirst().orElseThrow());

        Method handler = Arrays.stream(MixinItemInHandRenderer.class.getDeclaredMethods()).filter(method -> method.getName().equals("before_submitArmWithItem_JustZoom")).findFirst().orElseThrow();
        Inject inject = handler.getAnnotation(Inject.class);
        assertNotNull(inject);
        assertEquals(1, inject.method().length);
        assertEquals("submitArmWithItem", inject.method()[0]);
        assertEquals(1, inject.at().length);
        assertEquals("HEAD", inject.at()[0].value());
        assertTrue(inject.cancellable());
    }

}
