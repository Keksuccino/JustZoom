package de.keksuccino.justzoom;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class KeyBindings {

    public static KeyBinding keyToggleZoom;

    public static void init() {

        keyToggleZoom = new KeyBinding("justzoom.keybinds.keybind.zoom", 90, "justzoom.keybinds.category");

        ClientRegistry.registerKeyBinding(keyToggleZoom);

    }

}
