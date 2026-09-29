package de.keksuccino.justzoom.mixin.mixins.common.client;

import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractWidget.class)
public interface AccessorMixinAbstractWidget {

    @Accessor("alpha") float get_alpha_JustZoom();

}
