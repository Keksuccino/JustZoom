package de.keksuccino.justzoom;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyBinding keyToggleZoom;

    public static void init() {

        keyToggleZoom = new KeyBinding("Toggle Zoom", GLFW.GLFW_KEY_Z, "Just Zoom");
        ClientRegistry.registerKeyBinding(keyToggleZoom);

    }

}
