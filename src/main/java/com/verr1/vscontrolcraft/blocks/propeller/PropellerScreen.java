package com.verr1.vscontrolcraft.blocks.propeller;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PropellerScreen extends SimpleSettingScreen {

    private double thrust_ratio;
    private double torque_ratio;
    private BlockPos pos;

    public PropellerScreen(BlockPos entityPos, double thrust_ratio, double torque_ratio) {
        pos = entityPos;
        this.thrust_ratio = thrust_ratio;
        this.torque_ratio = torque_ratio;
    }



    @Override
    public void tick(){
        super.tick();
    }

    @Override
    public void startWindow() {
        addFieldWithLabel("T ratio", Util::tryParseDoubleFilter).setValue(String.format("%.4f", torque_ratio));
        addFieldWithLabel("F ratio", Util::tryParseDoubleFilter).setValue(String.format("%.4f", thrust_ratio));
    }

    public void register() {
        AllPackets
                .getChannel()
                .sendToServer(
                        new PropellerSettingsPacket(
                                pos,
                                false,
                                Util.tryParseDouble(iFields.get(1).getValue()),
                                Util.tryParseDouble(iFields.get(0).getValue())
                        )
                );
        onClose();
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.PROPELLER_BLOCK.asStack();
    }
}

