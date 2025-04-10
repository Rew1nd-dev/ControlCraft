package com.verr1.controlcraft.content.gui.widgets;


import com.mojang.blaze3d.systems.RenderSystem;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class SmallCheckbox extends AbstractButton {
    // private static final ResourceLocation TEXTURE_ON = new ResourceLocation("textures/gui/checkbox.png");
    private static final ControlCraftGuiTextures BUTTON_ON = ControlCraftGuiTextures.SMALL_BUTTON_GREEN;
    private static final ControlCraftGuiTextures BUTTON_OFF = ControlCraftGuiTextures.SMALL_BUTTON_RED;

    private static final int TEXT_COLOR = 14737632;
    private boolean selected;
    private final boolean showLabel;

    private Function<SmallCheckbox, Boolean> callback = (s)->{return false;};

    public SmallCheckbox(int p_93826_, int p_93827_, int p_93828_, int p_93829_, Component p_93830_, boolean p_93831_) {
        this(p_93826_, p_93827_, p_93828_, p_93829_, p_93830_, p_93831_, true);
    }

    public SmallCheckbox(int p_93833_, int p_93834_, int p_93835_, int p_93836_, Component p_93837_, boolean p_93838_, boolean p_93839_) {
        super(p_93833_, p_93834_, p_93835_, p_93836_, p_93837_);
        this.selected = p_93838_;
        this.showLabel = p_93839_;
    }

    public SmallCheckbox withCallback(Function<SmallCheckbox, Boolean> callback){
        this.callback = callback;
        return this;
    }

    public void onPress() {
        boolean cancel = callback.apply(this);
        if(cancel)return;
        this.selected = !this.selected;
    }

    public boolean selected() {
        return this.selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public void updateWidgetNarration(NarrationElementOutput p_260253_) {
        p_260253_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                p_260253_.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                p_260253_.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }

    }

    public void renderWidget(GuiGraphics graphics, int p_282925_, int mouse_x, float mouse_y) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.enableDepthTest();
        Font font = minecraft.font;
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        if(selected){
            graphics.blit(BUTTON_ON.location, getX(), getY(), BUTTON_ON.startX, BUTTON_ON.startY, BUTTON_ON.width, BUTTON_ON.height);
        }else{
            graphics.blit(BUTTON_OFF.location, getX(), getY(), BUTTON_OFF.startX, BUTTON_OFF.startY, BUTTON_OFF.width, BUTTON_OFF.height);
        }

        // graphics.blit(TEXTURE_ON, this.getX(), this.getY(), this.isFocused() ? 20.0F : 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 64, 64);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.showLabel && isHovered && !getMessage().getString().isEmpty()) {
            // graphics.drawString(font, this.getMessage(), this.getX() + 24, this.getY() + (this.height - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
            graphics.renderComponentTooltip(font, List.of(this.getMessage()), p_282925_, mouse_x);
        }

    }
}
