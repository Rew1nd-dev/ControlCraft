package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerCycleModePacket;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerSettingsPacket;
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

import java.awt.*;

public class SpatialScreen extends SimpleSettingScreen {

    private final double offset;
    private final long protocol;
    private final BlockPos pos;

    public SpatialScreen(BlockPos entityPos, double offset, long protocol) {
        pos = entityPos;
        this.offset = offset;
        this.protocol = protocol;

    }


    @Override
    public void startWindow() {
        addFieldWithLabel("Distance", Util::tryParseDoubleFilter).setValue(String.format("%.4f", offset));
        addFieldWithLabel("Channel", Util::tryParseLongFilter).setValue(protocol + "");
    }


    @Override
    public void tick(){
        super.tick();
    }


    public void register() {
        AllPackets
                .getChannel()
                .sendToServer(
                        new SpatialSettingsPacket(
                                pos,
                                Util.tryParseDouble(iFields.get(0).getValue()),
                                Util.tryParseLong(iFields.get(1).getValue())
                        )
                );
        onClose();
    }



    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.SPATIAL_ANCHOR_BLOCK.asStack();
    }
}
