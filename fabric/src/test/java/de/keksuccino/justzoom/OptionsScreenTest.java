package de.keksuccino.justzoom;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionsScreenTest {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("justzoom", "test.options_screen"));

    @Test
    void detectsChangedKeyThatMatchesAnotherBinding() {
        KeyMapping zoom = keyMapping("changed_zoom", InputConstants.KEY_X);
        KeyMapping other = keyMapping("other_zoom", InputConstants.KEY_Z);
        zoom.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_Z));

        assertTrue(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom, other}));
    }

    @Test
    void detectsDefaultKeyThatMatchesAnotherChangedBinding() {
        KeyMapping zoom = keyMapping("default_zoom", InputConstants.KEY_Z);
        KeyMapping other = keyMapping("changed_other", InputConstants.KEY_X);
        other.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_Z));

        assertTrue(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom, other}));
    }

    @Test
    void ignoresMatchingDefaultBindingsUntilOneIsChanged() {
        KeyMapping first = keyMapping("first_default", InputConstants.KEY_Z);
        KeyMapping second = keyMapping("second_default", InputConstants.KEY_Z);

        assertFalse(OptionsScreen.hasKeybindCollision(first, new KeyMapping[]{first, second}));
    }

    @Test
    void ignoresDifferentBindings() {
        KeyMapping zoom = keyMapping("different_zoom", InputConstants.KEY_Z);
        KeyMapping other = keyMapping("different_other", InputConstants.KEY_X);

        assertFalse(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom, other}));
    }

    @Test
    void ignoresUnboundBindings() {
        KeyMapping zoom = keyMapping("unbound_zoom", InputConstants.KEY_Z);
        KeyMapping other = keyMapping("unbound_other", InputConstants.KEY_X);
        zoom.setKey(InputConstants.UNKNOWN);

        assertFalse(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom, other}));
    }

    @Test
    void ignoresTheBindingItself() {
        KeyMapping zoom = keyMapping("self_zoom", InputConstants.KEY_Z);
        zoom.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_X));

        assertFalse(OptionsScreen.hasKeybindCollision(zoom, new KeyMapping[]{zoom}));
    }

    @Test
    void floatInputFillsTheRemainingOptionRowWidth() {
        OptionsScreen.FloatInputWidths widths = OptionsScreen.calculateFloatInputWidths(360, 100);

        assertEquals(100, widths.labelWidth());
        assertEquals(255, widths.inputWidth());
        assertEquals(360, widths.labelWidth() + OptionsScreen.FLOAT_INPUT_GAP + widths.inputWidth());
    }

    @Test
    void longFloatInputLabelLeavesTheInputUsable() {
        OptionsScreen.FloatInputWidths widths = OptionsScreen.calculateFloatInputWidths(100, 100);

        assertEquals(55, widths.labelWidth());
        assertEquals(OptionsScreen.FLOAT_INPUT_MIN_WIDTH, widths.inputWidth());
        assertEquals(100, widths.labelWidth() + OptionsScreen.FLOAT_INPUT_GAP + widths.inputWidth());
    }

    @Test
    void spyglassOverlayCycleUsesOrangeExceptWhenDisabled() {
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.ONLY_SPYGLASS));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.ONLY_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.SPYGLASS_AND_KEYBIND_ZOOM));
        assertEquals(OptionsScreen.DISABLED_CYCLE_VALUE_COLOR, OptionsScreen.spyglassOverlayValueColor(SpyglassOverlayMode.DISABLED));
    }

    @Test
    void controlsTabContainsEveryJustZoomKeybind() {
        List<KeyMapping> keyMappings = OptionsScreen.KEYBIND_SETTINGS.stream().map(OptionsScreen.KeybindSetting::keyMapping).toList();

        assertEquals(List.of(KeyMappings.KEY_TOGGLE_ZOOM, KeyMappings.KEY_ZOOM_IN, KeyMappings.KEY_ZOOM_OUT), keyMappings);
    }

    private static KeyMapping keyMapping(String suffix, int defaultKey) {
        return new KeyMapping("justzoom.test." + suffix, defaultKey, CATEGORY);
    }

}
