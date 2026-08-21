package de.keksuccino.justzoom;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public final class KeyMappings {

    // InputConstants serializes arbitrary mouse values as key.mouse.N. The values after GLFW's last real button can therefore
    // represent wheel directions without colliding with physical button events.
    private static final int MOUSE_WHEEL_UP_KEY_VALUE = InputConstants.MOUSE_BUTTON_8 + 1;
    private static final int MOUSE_WHEEL_DOWN_KEY_VALUE = InputConstants.MOUSE_BUTTON_8 + 2;

    public static final Identifier JUST_ZOOM_KEYMAPPING_CATEGORY_ID = Identifier.fromNamespaceAndPath("justzoom", "keybind.category.main");
    public static final KeyMapping.Category JUST_ZOOM_KEYMAPPING_CATEGORY = KeyMapping.Category.register(JUST_ZOOM_KEYMAPPING_CATEGORY_ID);

    public static final KeyMapping KEY_TOGGLE_ZOOM = new KeyMapping("justzoom.keybinds.keybind.zoom", InputConstants.KEY_Z, JUST_ZOOM_KEYMAPPING_CATEGORY);
    public static final KeyMapping KEY_ZOOM_IN = new KeyMapping("justzoom.keybinds.keybind.zoom_in", InputConstants.Type.MOUSE, MOUSE_WHEEL_UP_KEY_VALUE, JUST_ZOOM_KEYMAPPING_CATEGORY);
    public static final KeyMapping KEY_ZOOM_OUT = new KeyMapping("justzoom.keybinds.keybind.zoom_out", InputConstants.Type.MOUSE, MOUSE_WHEEL_DOWN_KEY_VALUE, JUST_ZOOM_KEYMAPPING_CATEGORY);

    private KeyMappings() {
    }

    public static boolean isZoomAdjustment(@NotNull KeyMapping keyMapping) {
        return keyMapping == KEY_ZOOM_IN || keyMapping == KEY_ZOOM_OUT;
    }

    public static boolean hasMouseWheelDirection(double deltaY) {
        return Double.isFinite(deltaY) && deltaY != 0.0D;
    }

    public static boolean matchesMouseWheel(@NotNull KeyMapping keyMapping, double deltaY) {
        if (!hasMouseWheelDirection(deltaY)) return false;
        return keyMapping.matches(getMouseWheelKey(deltaY));
    }

    @NotNull
    public static InputConstants.Key getMouseWheelKey(double deltaY) {
        if (!Double.isFinite(deltaY)) return InputConstants.UNKNOWN;
        if (deltaY > 0.0D) return InputConstants.Type.MOUSE.getOrCreate(MOUSE_WHEEL_UP_KEY_VALUE);
        if (deltaY < 0.0D) return InputConstants.Type.MOUSE.getOrCreate(MOUSE_WHEEL_DOWN_KEY_VALUE);
        return InputConstants.UNKNOWN;
    }

}
