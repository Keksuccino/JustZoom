package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MixinWidgetOpacityTargetTest {

    private static final String BLIT_SPRITE_TARGET = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V";

    @Test
    void opacityMixinsTargetTheExistingFourCoordinateBlitSpriteOverload() {
        assertDoesNotThrow(() -> GuiGraphicsExtractor.class.getDeclaredMethod("blitSprite", RenderPipeline.class, Identifier.class, int.class, int.class, int.class, int.class));
        assertWrapTarget(MixinEditBox.class, "wrap_blitSprite_in_extractWidgetRenderState_JustZoom");
        assertWrapTarget(MixinMenuTabButton.class, "wrap_blitSprite_in_extractWidgetRenderState_JustZoom");
    }

    private static void assertWrapTarget(Class<?> mixinClass, String handlerName) {
        Method handler = Arrays.stream(mixinClass.getDeclaredMethods()).filter(method -> method.getName().equals(handlerName)).findFirst().orElseThrow();
        WrapOperation wrapOperation = handler.getAnnotation(WrapOperation.class);
        assertNotNull(wrapOperation);
        assertEquals(1, wrapOperation.at().length);
        assertEquals(BLIT_SPRITE_TARGET, wrapOperation.at()[0].target());
    }

}
