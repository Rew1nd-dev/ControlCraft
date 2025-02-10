package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
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
        addNumericFieldWithLabel("Distance", Util::tryParseDoubleFilter).setValue(String.format("%.4f", offset));
        addNumericFieldWithLabel("Channel", Util::tryParseLongFilter).setValue(protocol + "");
        addBooleanFieldWithLabel("isRunning").setSelected(isRunning);
        addBooleanFieldWithLabel("isStatic").setSelected(isStatic);

    }


    @Override
    public void tick(){
        super.tick();
    }


    public void register() {
        var settingPacket = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING)
                .withDouble(Util.tryParseDouble(iFields.get(0).getValue()))
                .withLong(Util.tryParseLong(iFields.get(1).getValue()))
                .withBoolean(bFields.get(0).selected())
                .withBoolean(bFields.get(1).selected())
                .build();
        /*
        AllPackets
                .getChannel()
                .sendToServer(
                        new SpatialSettingsPacket(
                                pos,
                                Util.tryParseDouble(iFields.get(0).getValue()),
                                Util.tryParseLong(iFields.get(1).getValue()),
                                bFields.get(0).selected(),
                                bFields.get(1).selected()
                        )
                );
        * */

        AllPackets.getChannel().sendToServer(settingPacket);

        onClose();
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }


    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.SPATIAL_ANCHOR_BLOCK.asStack();
    }
}
