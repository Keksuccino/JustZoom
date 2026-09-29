package de.keksuccino.justzoom;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/** Kept separate so dedicated-server initialization never resolves client-only config screen classes. */
public final class ForgeClient {

    private ForgeClient() {
    }

    public static void setupModsScreenIntegration() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new OptionsScreen(parent)));
    }

}
