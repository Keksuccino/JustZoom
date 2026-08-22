package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.NotNull;

@Mod(JustZoom.MOD_ID)
public class JustZoomNeoForge {
    
    public JustZoomNeoForge(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer) {

        // JustZoom.init() got moved to MixinMinecraft

        if (Services.PLATFORM.isOnClient()) {
            eventBus.register(JustZoomNeoForge.class);
            IConfigScreenFactory configScreenFactory = (container, parent) -> new OptionsScreen(parent);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
        }

    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent e) {

        if (Services.PLATFORM.isOnClient()) {
            e.register(KeyMappings.KEY_TOGGLE_ZOOM);
            e.register(KeyMappings.KEY_ZOOM_IN);
            e.register(KeyMappings.KEY_ZOOM_OUT);
        }

    }

}
