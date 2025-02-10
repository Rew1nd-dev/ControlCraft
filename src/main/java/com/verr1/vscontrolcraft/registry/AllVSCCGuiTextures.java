package com.verr1.vscontrolcraft.registry;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.utility.Color;

import com.verr1.vscontrolcraft.ControlCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum AllVSCCGuiTextures implements ScreenElement {


    SIMPLE_BACKGROUND("simple_background", 176, 108),
    SIMPLE_BACKGROUND_HALF("simple_background_half", 87, 108),
    SIMPLE_BACKGROUND_QUARTER("simple_background_quarter", 114, 108),
    SIMPLE_BACKGROUND_LARGE("simple_background_large", 256, 133),


    SMALL_BUTTON_RED("icons10x10", 0, 0, 10, 10),
    SMALL_BUTTON_GREEN("icons10x10", 10, 0, 10, 10),
    ;

    public static final int FONT_COLOR = 0x575F7A;

    public final ResourceLocation location;
    public int width, height;
    public int startX, startY;

    private AllVSCCGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    private AllVSCCGuiTextures(int startX, int startY) {
        this("icons", startX * 16, startY * 16, 16, 16);
    }

    private AllVSCCGuiTextures(String location, int startX, int startY, int width, int height) {
        this(ControlCraft.MODID, location, startX, startY, width, height);
    }

    private AllVSCCGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @OnlyIn(Dist.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, location);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color c) {
        bind();
        UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
    }

}

