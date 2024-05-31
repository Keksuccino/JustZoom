package de.keksuccino.justzoom;

import de.keksuccino.justzoom.platform.Services;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JustZoom.MOD_ID)
public class JustZoomForge {
    
    public JustZoomForge() {

        JustZoom.init();

        if (Services.PLATFORM.isOnClient()) {

            FMLJavaModLoadingContext.get().getModEventBus().register(JustZoomForge.class);

        }
        
    }

    @SubscribeEvent
    public static void onRegisterKeybinds(RegisterKeyMappingsEvent e) {

        e.register(KeyMappings.KEY_TOGGLE_ZOOM);

    }

}