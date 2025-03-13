package com.verr1.controlcraft.content.wand;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.controlcraft.foundation.type.WandGUIModesType;
import com.verr1.controlcraft.foundation.type.WandModesType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class ModeSelectionScreen extends Screen {
    public final String scrollToCycle = Component.translatable("wand.gui.cycle")
            .getString();
    public final String holdToFocus = "gui.toolmenu.focusKey";

    protected List<WandGUIModesType> modes;
    protected Consumer<WandGUIModesType> callback;
    public boolean focused;
    private float yOffset;
    protected int selection;
    private boolean initialized;

    protected int w;
    protected int h;

    public ModeSelectionScreen(List<WandGUIModesType> modes, Consumer<WandGUIModesType> callback) {
        super(Components.literal("Tool Selection"));
        this.minecraft = Minecraft.getInstance();
        this.modes = modes;
        this.callback = callback;
        focused = false;
        yOffset = 0;
        selection = 0;
        initialized = false;

        callback.accept(modes.get(selection));

        w = Math.max(modes.size() * 50 + 30, 220);
        h = 30;
    }

    public void setSelectedElement(WandGUIModesType mode) {
        if (!modes.contains(mode))
            return;
        selection = modes.indexOf(mode);
    }

    public void cycle(int direction) {
        selection += (direction < 0) ? 1 : -1;
        selection = (selection + modes.size()) % modes.size();
    }

    private void draw(GuiGraphics graphics, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        if(minecraft == null)return;
        Window mainWindow = minecraft.getWindow();
        if (!initialized)
            init(minecraft, mainWindow.getGuiScaledWidth(), mainWindow.getGuiScaledHeight());

        int x = (mainWindow.getGuiScaledWidth() - w) / 2 + 15;
        int y = mainWindow.getGuiScaledHeight() - h - 75;

        matrixStack.pushPose();
        matrixStack.translate(0, -yOffset, focused ? 100 : 0);

        AllGuiTextures gray = AllGuiTextures.HUD_BACKGROUND;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, focused ? 7 / 8f : 1 / 2f);

        graphics.blit(gray.location, x - 15, y, gray.startX, gray.startY, w, h, gray.width, gray.height);

        float toolTipAlpha = yOffset / 10;
        List<Component> toolTip = modes.get(selection)
                .getDescription();
        int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;

        if (toolTipAlpha > 0.25f) {
            RenderSystem.setShaderColor(.7f, .7f, .8f, toolTipAlpha);
            graphics.blit(gray.location, x - 15, y + 33, gray.startX, gray.startY, w, h + 22, gray.width, gray.height);
            RenderSystem.setShaderColor(1, 1, 1, 1);

            if (toolTip.size() > 0)
                graphics.drawString(font, toolTip.get(0), x - 10, y + 38, 0xEEEEEE + stringAlphaComponent, false);
            if (toolTip.size() > 1)
                graphics.drawString(font, toolTip.get(1), x - 10, y + 50, 0xCCDDFF + stringAlphaComponent, false);
            if (toolTip.size() > 2)
                graphics.drawString(font, toolTip.get(2), x - 10, y + 60, 0xCCDDFF + stringAlphaComponent, false);
            if (toolTip.size() > 3)
                graphics.drawString(font, toolTip.get(3), x - 10, y + 72, 0xCCCCDD + stringAlphaComponent, false);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (modes.size() > 1) {
            String keyName = AllKeys.TOOL_MENU.getBoundKey();
            int width = minecraft.getWindow()
                    .getGuiScaledWidth();
            if (!focused)
                graphics.drawCenteredString(minecraft.font, Component.translatable("wand.gui.cycle").getString(), width / 2,
                        y - 10, 0xCCDDFF);
            else
                graphics.drawCenteredString(minecraft.font, Component.translatable("wand.gui.cycle").getString(), width / 2, y - 10, 0xCCDDFF);
        } else {
            x += 65;
        }


        for (int i = 0; i < modes.size(); i++) {
            RenderSystem.enableBlend();
            matrixStack.pushPose();

            float alpha = focused ? 1 : .2f;
            if (i == selection) {
                matrixStack.translate(0, -10, 0);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                graphics.drawCenteredString(minecraft.font, modes.get(i)
                                .getID()
                        , x + i * 50 + 24, y + 28, 0xCCDDFF);
                alpha = 1;
            }
            RenderSystem.setShaderColor(0, 0, 0, alpha);
            modes.get(i)
                    .getIcon()
                    .render(graphics, x + i * 50 + 16, y + 12);
            RenderSystem.setShaderColor(1, 1, 1, alpha);
            modes.get(i)
                    .getIcon()
                    .render(graphics, x + i * 50 + 16, y + 11);

            matrixStack.popPose();
        }

        RenderSystem.disableBlend();
        matrixStack.popPose();
    }

    public void update() {
        if (focused)
            yOffset += (10 - yOffset) * .1f;
        else
            yOffset *= .9f;
    }

    public void renderPassive(GuiGraphics graphics, float partialTicks) {
        draw(graphics, partialTicks);
    }

    @Override
    public void onClose() {
        callback.accept(modes.get(selection));
    }

    @Override
    protected void init() {
        super.init();
        initialized = true;
    }
}
