package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.OptionsScreen;
import de.keksuccino.justzoom.util.gui.ItemButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Supplier;

@Mixin(PauseScreen.class)
public class MixinPauseScreen extends Screen {

    @Shadow @Final private boolean showPauseMenu;

    //unused dummy constructor
    private MixinPauseScreen() {
        super(Component.empty());
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void return_init_JustZoom(CallbackInfo info) {

        if (this.showPauseMenu) {

            this.addRenderableWidget(new ItemButton(20, this.height - 40, Component.translatable("justzoom.options"), button -> {
                        Minecraft.getInstance().setScreen(new OptionsScreen(this));
                    }, Supplier::get, new ItemStack(Items.SPYGLASS)))
                    .setItemPositionOffset(2, 2)
                    .setTooltip(Tooltip.create(Component.translatable("justzoom.options")));

        }

    }

}
