package com.verr1.vscontrolcraft.blocks.magnet;

import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
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
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.STRENGTH.getComponent(), Util::tryParseDoubleFilter)
                .setValue(String.format("%.1f", s)); // iFields[0]
    }

    @Override
    public void init() {
        super.init();
        redstoneSettings.visible = false;
    }

    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING)
                .withDouble(s)
                .build();
        AllPackets
                .getChannel()
                .sendToServer(p);
        onClose();
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.MAGNET_BLOCK.asStack();
    }
}
