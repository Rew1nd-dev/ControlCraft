package com.verr1.controlcraft.content.gui.widgets;

import com.simibubi.create.foundation.gui.widget.IconButton;
import com.verr1.controlcraft.content.gui.layouts.api.SizedScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SmallIconButton extends IconButton {

    public SmallIconButton(int x, int y, SizedScreenElement icon) {
        super(x, y, icon.width(), icon.height(), icon);
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.icon.render(graphics, this.getX() + 1, this.getY() + 1);
        }
    }

    public SmallIconButton withToolTips(List<Component> tooltips) {
        this.getToolTip().clear();
        this.getToolTip().addAll(tooltips);
        return this;

    }

}
