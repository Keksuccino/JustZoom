package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JustZoom.MOD_ID)
public class JustZoomForge {
    
    public JustZoomForge() {

        JustZoom.init();

        if (Services.PLATFORM.isOnClient()) {

            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientInit);

        }
        
    }

    private void doClientInit(final FMLClientSetupEvent e) {
        ClientRegistry.registerKeyBinding(KeyMappings.KEY_TOGGLE_ZOOM);
    }

}