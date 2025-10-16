package de.keksuccino.justzoom.mixin.mixins.common.client;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(KeyMapping.Category.class)
public interface IMixinKeyMapping_Category {

    @Accessor("SORT_ORDER") static List<KeyMapping.Category> get_SORT_ORDER_JustZoom() { return null; }

}
