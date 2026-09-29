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
            KeyBindingHelper.registerKeyBinding(KeyMappings.KEY_ZOOM_IN);
            KeyBindingHelper.registerKeyBinding(KeyMappings.KEY_ZOOM_OUT);
            KeyBindingHelper.registerKeyBinding(KeyMappings.KEY_OPEN_OPTIONS);

        }

    }

}
