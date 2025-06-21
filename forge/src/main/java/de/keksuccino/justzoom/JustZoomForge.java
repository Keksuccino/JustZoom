package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;

@Mod(JustZoom.MOD_ID)
public class JustZoomForge {
    
    public JustZoomForge() {

        // JustZoom.init() got moved to MixinMinecraft

        if (Services.PLATFORM.isOnClient()) {

            RegisterKeyMappingsEvent.getBus(BusGroup.DEFAULT).addListener(this::onRegisterKeybinds);

        }
        
    }

    public void onRegisterKeybinds(RegisterKeyMappingsEvent e) {

        e.register(KeyMappings.KEY_TOGGLE_ZOOM);

    }

}