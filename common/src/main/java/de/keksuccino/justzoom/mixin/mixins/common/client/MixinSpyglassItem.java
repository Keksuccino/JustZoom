package de.keksuccino.justzoom.mixin.mixins.common.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.keksuccino.justzoom.SpyglassSoundHandler;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpyglassItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpyglassItem.class)
public class MixinSpyglassItem {

    @WrapWithCondition(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private boolean wrap_playUseSound_JustZoom(Player instance, SoundEvent sound, float volume, float pitch) {
        return SpyglassSoundHandler.shouldPlaySpyglassSounds(instance);
    }

    @WrapWithCondition(method = "stopUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private boolean wrap_playStopUsingSound_JustZoom(LivingEntity instance, SoundEvent sound, float volume, float pitch) {
        return SpyglassSoundHandler.shouldPlaySpyglassSounds(instance);
    }

}
