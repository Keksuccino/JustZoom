package de.keksuccino.justzoom.mixin.mixins.common.client;

import de.keksuccino.justzoom.JustZoom;
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
        if (this.showPauseMenu && JustZoom.getOptions().showOptionsButtonInPauseScreen.getValue()) {
            int buttonX, buttonY;
            // Get corner option from JustZoom.getOptions()
            int corner = JustZoom.getOptions().optionsButtonCorner.getValue();

            switch (corner) {
                case 1: // BOTTOM_RIGHT
                    buttonX = this.width - 40;
                    buttonY = this.height - 40;
                    break;
                case 2: // TOP_LEFT
                    buttonX = 20;
                    buttonY = 20;
                    break;
                case 3: // TOP_RIGHT
                    buttonX = this.width - 40;
                    buttonY = 20;
                    break;
                case 0: // BOTTOM_LEFT
                default:
                    buttonX = 20;
                    buttonY = this.height - 40;
                    break;
            }

            this.addRenderableWidget(new ItemButton(buttonX, buttonY, Component.translatable("justzoom.options"), button -> {
                        Minecraft.getInstance().setScreen(new OptionsScreen(this));
                    }, Supplier::get, new ItemStack(Items.SPYGLASS)))
                    .setItemPositionOffset(2, 2)
                    .setTooltip(Tooltip.create(Component.translatable("justzoom.options")));
        }
    }

}