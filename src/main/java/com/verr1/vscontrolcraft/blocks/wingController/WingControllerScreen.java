package com.verr1.vscontrolcraft.blocks.wingController;

import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class WingControllerScreen extends SimpleSettingScreen {
    private final double angle;
    private final BlockPos pos;

    public WingControllerScreen(BlockPos pos, double angle) {
        this.angle = angle;
        this.pos = pos;
    }


    @Override
    public void startWindow() {
        addNumericFieldWithLabel(Component.translatable("vscontrolcraft.screen.labels.field.degree"), Util::tryParseDoubleFilter)
                .setValue(String.format("%.2f", angle));
    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING_0)
                .withDouble(Util.tryParseDouble(iFields.get(0).getValue()))
                .build();
        AllPackets.getChannel().sendToServer(p);
        onClose();
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.WING_CONTROLLER_BLOCK.asStack();
    }
}
