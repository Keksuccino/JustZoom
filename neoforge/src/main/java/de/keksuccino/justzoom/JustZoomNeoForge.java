package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.NotNull;

@Mod(JustZoom.MOD_ID)
public class JustZoomNeoForge {

    public JustZoomNeoForge(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer) {

        JustZoom.init();

        if (Services.PLATFORM.isOnClient()) {

            eventBus.register(JustZoomNeoForge.class);
            NeoForgeClient.setupModsScreenIntegration(modContainer);

        }

    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent e) {

        e.register(KeyMappings.KEY_TOGGLE_ZOOM);
        e.register(KeyMappings.KEY_ZOOM_IN);
        e.register(KeyMappings.KEY_ZOOM_OUT);
        e.register(KeyMappings.KEY_OPEN_OPTIONS);

    }

}
