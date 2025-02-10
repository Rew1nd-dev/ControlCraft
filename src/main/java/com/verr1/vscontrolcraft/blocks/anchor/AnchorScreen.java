package com.verr1.vscontrolcraft.blocks.anchor;

import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
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
        addNumericFieldWithLabel("Air Resist", Util::tryParseDoubleFilter)
                .setValue(String.format("%.4f", airResistance)); // iFields[0]
        addNumericFieldWithLabel("Extra G", Util::tryParseDoubleFilter)
                .setValue(String.format("%.4f", extraGravity));    // iFields[1]

    }


    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING)
                .withDouble(Util.tryParseDouble(iFields.get(0).getValue()))
                .withDouble(Util.tryParseDouble(iFields.get(1).getValue()))
                .build();
        AllPackets
                .getChannel()
                .sendToServer(p);
        onClose();
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.ANCHOR_BLOCK.asStack();
    }
}
