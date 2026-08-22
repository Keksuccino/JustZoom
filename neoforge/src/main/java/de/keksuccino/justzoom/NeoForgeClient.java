package de.keksuccino.justzoom;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

public class NeoForgeClient {

    public static void setupModsScreenIntegration(@NotNull ModContainer modContainer) {
        IConfigScreenFactory configScreenFactory = (container, parent) -> new OptionsScreen(parent);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
    }

}
