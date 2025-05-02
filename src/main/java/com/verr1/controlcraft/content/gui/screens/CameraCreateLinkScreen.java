package com.verr1.controlcraft.content.gui.screens;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.verr1.controlcraft.content.gui.widgets.SmallIconButton;
import com.verr1.controlcraft.content.gui.layouts.api.SizedScreenElement;
import com.verr1.controlcraft.foundation.type.descriptive.MiscDescription;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CameraCreateLinkScreen extends AbstractSimiScreen {

    private final BlockPos pos;
    private final SizedScreenElement background = ControlCraftGuiTextures.SIMPLE_BACKGROUND_ONE_LINE;
    private final IconButton dump = new SmallIconButton(0, 0, ControlCraftGuiTextures.SMALL_BUTTON_NO)
            .withToolTips(MiscDescription.DUMP.specific()).withCallback(this::dump);
    private final EditBox name = new EditBox(
            Minecraft.getInstance().font, 0, 0, 120, 10, Component.literal("Camera Link Name")
    );

    public CameraCreateLinkScreen(BlockPos pos) {
        this.pos = pos;
    }


    @Override
    protected void init() {
        setWindowSize(background.width(), background.height());
        super.init();


        name.setX(guiLeft + 10);
        name.setY(guiTop + 5);

        dump.setX(guiLeft + windowWidth - 20);
        dump.setY(guiTop + 4);

        addRenderableWidgets(dump, name);

    }

    @Override
    public void onClose() {
        super.onClose();
        confirm();
    }

    public void dump(){
        onClose();
    }

    private void confirm(){
        CameraLinkScreen.AddLink(pos, name.getValue());

    }


    @Override
    protected void renderWindow(GuiGraphics graphics, int i, int i1, float v) {
        background.render(graphics, guiLeft, guiTop);
    }
}
