package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class JustZoomFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        JustZoom.init();

        if (Services.PLATFORM.isOnClient()) {

            KeyMappingHelper.registerKeyMapping(KeyMappings.KEY_TOGGLE_ZOOM);

        }

    }

}
