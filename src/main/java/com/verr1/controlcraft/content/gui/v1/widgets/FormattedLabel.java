package com.verr1.controlcraft.content.gui.v1.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widget.Label;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.UnaryOperator;

public class FormattedLabel extends Label {



    public FormattedLabel(int x, int y, Component text) {
        super(x, y, text);
    }

    public FormattedLabel withToolTip(Component tooltip){
        this.toolTip.add(tooltip);
        return this;
    }

    public FormattedLabel withToolTips(Collection<Component> tooltip){
        this.toolTip.addAll(tooltip);
        return this;
    }

    public FormattedLabel withTextStyle(UnaryOperator<Style> style){
        setText(this.text.copy().withStyle(style));
        return this;
    }

    public void setText(Component text){
        this.text = text;
        setWidth(font.width(text));
    }

    public void setTextOnly(Component text){
        this.text = text;
    }

    @Override
    protected void doRender(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.text != null && !this.text.getString().isEmpty()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            MutableComponent copy = this.text.copy();
            if (this.suffix != null && !this.suffix.isEmpty()) {
                copy.append(this.suffix);
            }

            graphics.drawString(this.font, copy, this.getX(), this.getY(), this.color, this.hasShadow);
        }

    }
}
