package com.verr1.controlcraft.content.gui;

import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AnchorScreen extends SimpleSettingScreen {


    private final double airResistance;
    private final double extraGravity;
    private final double rotationalResistance;
    private final BlockPos pos;

    public AnchorScreen(BlockPos entityPos, double airResistance, double extraGravity, double rotationalResistance) {
        pos = entityPos;
        this.airResistance = airResistance;
        this.extraGravity = extraGravity;
        this.rotationalResistance = rotationalResistance;
    }


    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.AIR_RESISTANCE.getComponent(), ParseUtils::tryParseDoubleFilter)
                .setValue(String.format("%.4f", airResistance)); // iFields[0]
        addNumericFieldWithLabel(ExposedFieldType.EXTRA_GRAVITY.getComponent(), ParseUtils::tryParseDoubleFilter)
                .setValue(String.format("%.4f", extraGravity));    // iFields[1]
        addNumericFieldWithLabel(ExposedFieldType.ROTATIONAL_RESISTANCE.getComponent(), ParseUtils::tryParseDoubleFilter)
                .setValue(String.format("%.4f", rotationalResistance));

    }

    @Override
    public void init() {
        super.init();
        redstoneSettings.visible = false;
    }

    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.SETTING_0)
                .withDouble(ParseUtils.tryParseDouble(iFields.get(0).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(1).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(2).getValue()))
                .build();
        ControlCraftPackets
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
        return ControlCraftBlocks.ANCHOR_BLOCK.asStack();
    }
}
