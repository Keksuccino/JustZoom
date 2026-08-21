package de.keksuccino.justzoom.mixin.mixins.common.client;

import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBindsScreen.class)
public interface AccessorMixinKeyBindsScreen {

    @Accessor("keyBindsList")
    KeyBindsList get_keyBindsList_JustZoom();

}
