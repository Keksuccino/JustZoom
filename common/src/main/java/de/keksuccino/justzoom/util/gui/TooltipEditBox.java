package de.keksuccino.justzoom.util.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TooltipEditBox extends EditBox {

    @Nullable
    protected Button.OnTooltip tooltip;

    public TooltipEditBox(Font $$0, int $$1, int $$2, int $$3, int $$4, Component $$5) {
        super($$0, $$1, $$2, $$3, $$4, $$5);
    }

    public TooltipEditBox(Font $$0, int $$1, int $$2, int $$3, int $$4, @Nullable EditBox $$5, Component $$6) {
        super($$0, $$1, $$2, $$3, $$4, $$5, $$6);
    }

    @Override
    public void render(PoseStack $$0, int $$1, int $$2, float $$3) {
        super.render($$0, $$1, $$2, $$3);
        if (this.isHovered) {
            this.renderToolTip($$0, $$1, $$2);
        }
    }

    @Override
    public void renderToolTip(@NotNull PoseStack $$0, int $$1, int $$2) {
        if (this.tooltip != null) {
            this.tooltip.onTooltip(new Button(this.x, this.y, this.width, this.height, Components.empty(), var1 -> {}), $$0, $$1, $$2);
        }
    }

    public void setTooltip(@Nullable Button.OnTooltip tooltip) {
        this.tooltip = tooltip;
    }

}
