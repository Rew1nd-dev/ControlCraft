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

public class SpatialScreen extends SimpleSettingScreen {

    private final double offset;
    private final long protocol;
    private final boolean isStatic;
    private final boolean isRunning;
    private final BlockPos pos;

    public SpatialScreen(BlockPos entityPos, double offset, long protocol, boolean isRunning, boolean isStatic) {
        pos = entityPos;
        this.offset = offset;
        this.protocol = protocol;
        this.isRunning = isRunning;
        this.isStatic = isStatic;
    }


    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.OFFSET.getComponent(), ParseUtils::tryParseDoubleFilter).setValue(String.format("%.4f", offset));
        addNumericFieldWithLabel(ExposedFieldType.PROTOCOL.getComponent(), ParseUtils::tryParseLongFilter).setValue(protocol + "");
        addBooleanFieldWithLabel(ExposedFieldType.IS_RUNNING.getComponent()).setSelected(isRunning);
        addBooleanFieldWithLabel(ExposedFieldType.IS_STATIC.getComponent()).setSelected(isStatic);

    }


    @Override
    public void tick(){
        super.tick();
    }


    public void register() {
        var settingPacket = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.SETTING_0)
                .withDouble(ParseUtils.tryParseDouble(iFields.get(0).getValue()))
                .withLong(ParseUtils.tryParseLong(iFields.get(1).getValue()))
                .withBoolean(bFields.get(0).selected())
                .withBoolean(bFields.get(1).selected())
                .build();

        ControlCraftPackets.getChannel().sendToServer(settingPacket);

        onClose();
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }


    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.SPATIAL_ANCHOR_BLOCK.asStack();
    }
}
