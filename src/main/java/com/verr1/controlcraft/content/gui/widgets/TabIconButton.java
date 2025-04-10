package com.verr1.controlcraft.content.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.verr1.controlcraft.content.gui.layouts.TabSwitch;
import com.verr1.controlcraft.content.gui.layouts.api.SwitchableTab;
import com.verr1.controlcraft.foundation.api.SizedScreenElement;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TabIconButton extends IconButton {

    private final SwitchableTab boundTab;
    private final TabSwitch manager;
    private final SizedScreenElement frame;
    private final SizedScreenElement sizedIcon;

    public TabIconButton(int x, int y, SizedScreenElement icon, SizedScreenElement frame, @NotNull SwitchableTab tab, @NotNull TabSwitch manager) {
        super(x, y, icon);
        this.boundTab = tab;
        this.sizedIcon = icon;
        this.manager = manager;
        this.frame = frame;
        withCallback(
                () -> this.manager.setCurrentTab(boundTab, true)
        );
    }

    public boolean shouldRenderFrame(){
        return manager.isCurrentTab(boundTab);
    }

    public TabIconButton withTooltip(List<Component> tooltip) {
        this.getToolTip().addAll(tooltip);
        return this;
    }

    protected void drawBackground(GuiGraphics graphics, ControlCraftGuiTextures button) {
        graphics.blit(button.location, this.getX(), this.getY(), button.startX, button.startY, button.width, button.height);
    }

    /*
    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        //super.doRender(graphics, mouseX, mouseY, partialTicks);
        if(!this.visible)return;
        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        ControlCraftGuiTextures bg = ControlCraftGuiTextures.TAB_BUTTON_BACKGROUND;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawBackground(graphics, bg);

        int center_x = this.getX() + this.width / 2;
        int center_y = this.getY() + this.height / 2;
        int icon_x = center_x - sizedIcon.width() / 2;
        int icon_y = center_y - sizedIcon.height() / 2;

        this.sizedIcon.render(graphics, icon_x, icon_y);

        if(!shouldRenderFrame())return;

        this.frame.render(graphics, this.getX(), this.getY() + height - 1);
    }
    * */

}
