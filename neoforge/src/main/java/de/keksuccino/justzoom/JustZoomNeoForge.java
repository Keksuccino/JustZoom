package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.NotNull;

@Mod(JustZoom.MOD_ID)
public class JustZoomNeoForge {
    
    public JustZoomNeoForge(@NotNull IEventBus eventBus) {

        JustZoom.init();

        if (Services.PLATFORM.isOnClient()) {

            eventBus.register(JustZoomNeoForge.class);

        }

    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent e) {

        e.register(KeyMappings.KEY_TOGGLE_ZOOM);

    }

}