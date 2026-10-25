package de.keksuccino.justzoom;

import de.keksuccino.justzoom.networking.FabricNetworking;
import de.keksuccino.justzoom.platform.Services;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class JustZoomFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        JustZoom.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> ServerZoomRules.onServerStarting());
        FabricNetworking.register();

        if (Services.PLATFORM.isOnClient()) {

            KeyMappingHelper.registerKeyMapping(KeyMappings.KEY_TOGGLE_ZOOM);
            KeyMappingHelper.registerKeyMapping(KeyMappings.KEY_ZOOM_IN);
            KeyMappingHelper.registerKeyMapping(KeyMappings.KEY_ZOOM_OUT);
            KeyMappingHelper.registerKeyMapping(KeyMappings.KEY_OPEN_OPTIONS);

            FabricNetworking.registerClient();

        }

    }

}
