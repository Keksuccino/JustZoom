package de.keksuccino.justzoom;

import de.keksuccino.justzoom.networking.NeoForgeNetworking;
import de.keksuccino.justzoom.platform.Services;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.NotNull;

@Mod(JustZoom.MOD_ID)
public class JustZoomNeoForge {

    public JustZoomNeoForge(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer) {

        JustZoom.init();

        NeoForge.EVENT_BUS.addListener(ServerAboutToStartEvent.class, event -> ServerZoomRules.onServerStarting());
        NeoForgeNetworking.register(eventBus);

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
