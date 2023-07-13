package de.keksuccino.justzoom;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;

public class KeyBindings {

    public static KeyMapping keyToggleZoom;

    public static void init() {

        keyToggleZoom = new KeyMapping("justzoom.keybinds.keybind.zoom", InputConstants.KEY_Z, "justzoom.keybinds.category");

        ClientRegistry.registerKeyBinding(keyToggleZoom);

    }

}
