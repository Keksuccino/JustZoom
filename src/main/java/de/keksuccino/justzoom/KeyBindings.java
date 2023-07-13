package de.keksuccino.justzoom;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class KeyBindings {

    public static KeyMapping keyToggleZoom;

    public static void init() {

        keyToggleZoom = new KeyMapping("justzoom.keybinds.keybind.zoom", InputConstants.KEY_Z, "justzoom.keybinds.category");

        FMLJavaModLoadingContext.get().getModEventBus().register(KeyBindings.class);

    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent e) {

        e.register(keyToggleZoom);

    }

}
