package de.keksuccino.justzoom;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyMapping keyToggleZoom;

    public static void init() {

        keyToggleZoom = new KeyMapping("Toggle Zoom", GLFW.GLFW_KEY_Z, "Just Zoom");

        FMLJavaModLoadingContext.get().getModEventBus().register(KeyBindings.class);

    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent e) {

        e.register(keyToggleZoom);

    }

}
