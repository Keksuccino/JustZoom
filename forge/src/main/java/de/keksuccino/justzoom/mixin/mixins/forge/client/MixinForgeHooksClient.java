package de.keksuccino.justzoom.mixin.mixins.forge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.justzoom.util.gui.PostRenderTasks;
import de.keksuccino.justzoom.util.gui.TooltipHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeHooksClient.class)
public class MixinForgeHooksClient {

    @Inject(method = "drawScreen", at = @At("RETURN"), remap = false)
    private static void after_drawScreen_JustZoom(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TooltipHandler.tick();
        PostRenderTasks.getAllAndClear().forEach(task -> task.render(poseStack, mouseX, mouseY, partialTick));
    }

}
