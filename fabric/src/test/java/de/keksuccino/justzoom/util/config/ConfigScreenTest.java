package de.keksuccino.justzoom.util.config;

import com.mojang.blaze3d.platform.InputConstants;
import de.keksuccino.justzoom.util.config.gui.ConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigScreenTest {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("justzoom", "test.config_screen"));

    @TempDir
    Path temporaryDirectory;

    @Test
    void detectsChangedKeyThatMatchesAnotherBinding() {
        KeyMapping changed = keyMapping("changed", InputConstants.KEY_X);
        KeyMapping other = keyMapping("other", InputConstants.KEY_Z);
        changed.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_Z));

        assertTrue(ConfigScreen.hasKeybindCollision(changed, new KeyMapping[]{changed, other}));
    }

    @Test
    void detectsDefaultKeyThatMatchesAnotherChangedBinding() {
        KeyMapping defaultMapping = keyMapping("default", InputConstants.KEY_Z);
        KeyMapping changed = keyMapping("changed_other", InputConstants.KEY_X);
        changed.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_Z));

        assertTrue(ConfigScreen.hasKeybindCollision(defaultMapping, new KeyMapping[]{defaultMapping, changed}));
    }

    @Test
    void ignoresMatchingDefaultBindingsUntilOneIsChanged() {
        KeyMapping first = keyMapping("first_default", InputConstants.KEY_Z);
        KeyMapping second = keyMapping("second_default", InputConstants.KEY_Z);

        assertFalse(ConfigScreen.hasKeybindCollision(first, new KeyMapping[]{first, second}));
    }

    @Test
    void ignoresDifferentUnboundAndSelfBindings() {
        KeyMapping first = keyMapping("first", InputConstants.KEY_Z);
        KeyMapping second = keyMapping("second", InputConstants.KEY_X);

        assertFalse(ConfigScreen.hasKeybindCollision(first, new KeyMapping[]{first, second}));
        assertFalse(ConfigScreen.hasKeybindCollision(first, new KeyMapping[]{first}));

        first.setKey(InputConstants.UNKNOWN);
        assertFalse(ConfigScreen.hasKeybindCollision(first, new KeyMapping[]{first, second}));
    }

    @Test
    void mouseWheelCaptureIsOptInForEachKeybind() {
        KeyMapping keyMapping = keyMapping("wheel", InputConstants.KEY_Z);
        InputConstants.Key wheelUp = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_8 + 1);
        ConfigScreen.KeybindSetting standard = new ConfigScreen.KeybindSetting(keyMapping, "test.label", "test.description");
        ConfigScreen.KeybindSetting wheelEnabled = new ConfigScreen.KeybindSetting(keyMapping, "test.label", "test.description", delta -> delta > 0.0D ? wheelUp : InputConstants.UNKNOWN);

        assertNull(standard.getMouseWheelKey(1.0D));
        assertEquals(wheelUp, wheelEnabled.getMouseWheelKey(1.0D));
        assertEquals(InputConstants.UNKNOWN, wheelEnabled.getMouseWheelKey(-1.0D));
    }

    @Test
    void optionDefaultStateTracksTheStoredValue() {
        ConfigValue<Integer> option = this.option("default_state", 75);

        assertTrue(ConfigScreen.isOptionDefault(option));

        option.setValue(50);
        assertFalse(ConfigScreen.isOptionDefault(option));

        option.resetToDefault();
        assertTrue(ConfigScreen.isOptionDefault(option));
    }

    @Test
    void historicalFloatInputAcceptsOnlyFiniteFloats() {
        assertEquals(Optional.of(0.25F), ConfigScreen.parseFloatInput("0.25"));
        assertEquals(Optional.of(-1.5F), ConfigScreen.parseFloatInput("-1.5"));
        assertEquals(Optional.empty(), ConfigScreen.parseFloatInput(""));
        assertEquals(Optional.empty(), ConfigScreen.parseFloatInput("-"));
        assertEquals(Optional.empty(), ConfigScreen.parseFloatInput("NaN"));
        assertEquals(Optional.empty(), ConfigScreen.parseFloatInput("Infinity"));
    }

    @Test
    void inputDefaultStateRequiresValidDefaultTextAndTheStoredDefault() {
        ConfigValue<Float> option = this.option("float_input", 0.25F);

        assertTrue(ConfigScreen.isInputDefault(option, ".25", ConfigScreen::parseFloatInput));
        assertFalse(ConfigScreen.isInputDefault(option, "invalid", ConfigScreen::parseFloatInput));
        assertFalse(ConfigScreen.isInputDefault(option, "0.5", ConfigScreen::parseFloatInput));

        option.setValue(0.5F);
        assertFalse(ConfigScreen.isInputDefault(option, "0.25", ConfigScreen::parseFloatInput));
    }

    @Test
    void inputWidthsPreserveTheRowWidthAndMinimumInputWhenPossible() {
        ConfigScreen.InputWidths preferred = ConfigScreen.calculateInputWidths(305, 100);
        ConfigScreen.InputWidths constrained = ConfigScreen.calculateInputWidths(100, 100);

        assertEquals(100, preferred.labelWidth());
        assertEquals(200, preferred.inputWidth());
        assertEquals(20, constrained.labelWidth());
        assertEquals(75, constrained.inputWidth());
    }

    private <T> ConfigValue<T> option(String key, T defaultValue) {
        JsonConfig config = new JsonConfig(this.temporaryDirectory.resolve(key + ".json"));
        return config.option(key, defaultValue);
    }

    private static KeyMapping keyMapping(String suffix, int defaultKey) {
        return new KeyMapping("justzoom.test." + suffix, defaultKey, CATEGORY);
    }

}
