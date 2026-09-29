package de.keksuccino.justzoom.util.config.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WidgetRenderUtilsTest {

    @Test
    void fadesOpaqueAndRgbOnlyTextColors() {
        assertEquals(0x33ABCDEF, WidgetRenderUtils.multiplyAlpha(0xFFABCDEF, 0.2F));
        assertEquals(0x33ABCDEF, WidgetRenderUtils.multiplyAlpha(0x00ABCDEF, 0.2F));
    }

    @Test
    void preservesExistingTransparencyAndClampsOpacity() {
        assertEquals(0x40123456, WidgetRenderUtils.multiplyAlpha(0x80123456, 0.5F));
        assertEquals(0x80123456, WidgetRenderUtils.multiplyAlpha(0x80123456, 2.0F));
        assertEquals(0x00123456, WidgetRenderUtils.multiplyAlpha(0x80123456, -1.0F));
    }

}
