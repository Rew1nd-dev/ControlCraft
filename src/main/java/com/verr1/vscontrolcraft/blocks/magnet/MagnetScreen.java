package com.verr1.vscontrolcraft.blocks.magnet;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MagnetScreen extends SimpleSettingScreen {


    private final double s;
    private final BlockPos pos;

    public MagnetScreen(BlockPos entityPos, double s) {
        pos = entityPos;
        this.s = s;
    }



    @Override
    public void startWindow() {
        addFieldWithLabel("Strength", Util::tryParseDoubleFilter)
                .setValue(String.format("%.4f", s)); // iFields[0]
    }



    public void register() {
        AllPackets
                .getChannel()
                .sendToServer(
                        new MagnetSettingsPacket(
                                Util.tryParseDouble(iFields.get(0).getValue()),
                                pos
                        )
                );
        onClose();
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.MAGNET_BLOCK.asStack();
    }
}
