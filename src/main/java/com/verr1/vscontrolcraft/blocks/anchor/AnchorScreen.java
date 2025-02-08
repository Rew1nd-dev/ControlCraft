package com.verr1.vscontrolcraft.blocks.anchor;

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

public class AnchorScreen extends SimpleSettingScreen {


    private final double airResistance;
    private final double extraGravity;
    private final BlockPos pos;

    public AnchorScreen(BlockPos entityPos, double airResistance, double extraGravity) {
        pos = entityPos;
        this.airResistance = airResistance;
        this.extraGravity = extraGravity;
    }


    @Override
    public void startWindow() {
        addFieldWithLabel("Air Resist", Util::tryParseDoubleFilter)
                .setValue(String.format("%.4f", airResistance)); // iFields[0]
        addFieldWithLabel("Extra G", Util::tryParseDoubleFilter)
                .setValue(String.format("%.4f", extraGravity));    // iFields[1]

    }


    public void register() {
        AllPackets
                .getChannel()
                .sendToServer(
                        new AnchorSettingsPacket(
                                Util.tryParseDouble(iFields.get(0).getValue()),
                                Util.tryParseDouble(iFields.get(1).getValue()),
                                pos
                        )
                );
        onClose();
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.ANCHOR_BLOCK.asStack();
    }
}
