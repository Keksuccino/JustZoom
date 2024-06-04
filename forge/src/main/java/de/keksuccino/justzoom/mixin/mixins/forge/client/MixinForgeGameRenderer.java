package de.keksuccino.justzoom.mixin.mixins.forge.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.justzoom.util.gui.ScreenRenderUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class MixinForgeGameRenderer {

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"))
    private void wrap_renderScreen_in_render_JustZoom(Screen screen, PoseStack pose, int mouseX, int mouseY, float partial, Operation<Void> original) {
        original.call(screen, pose, mouseX, mouseY, partial);
        ScreenRenderUtil.getContextsAndClearList().forEach(context -> context.render(pose, mouseX, mouseY, partial));
    }

}
