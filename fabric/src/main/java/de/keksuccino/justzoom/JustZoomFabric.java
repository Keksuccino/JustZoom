package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class JustZoomFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        JustZoom.init();

        if (Services.PLATFORM.isOnClient()) {

            KeyBindingHelper.registerKeyBinding(KeyMappings.KEY_TOGGLE_ZOOM);

        }

    }

}
