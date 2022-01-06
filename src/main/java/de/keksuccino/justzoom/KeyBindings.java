package de.keksuccino.justzoom;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyMapping keyToggleZoom;

    public static void init() {

        keyToggleZoom = new KeyMapping("Toggle Zoom", GLFW.GLFW_KEY_Z, "Just Zoom");
        ClientRegistry.registerKeyBinding(keyToggleZoom);

    }

}
