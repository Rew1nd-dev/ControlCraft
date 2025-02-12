package com.verr1.vscontrolcraft.blocks.propellerController;

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

public class PropellerControllerScreen extends SimpleSettingScreen {
    private final BlockPos pos;
    private final double speed;

    public PropellerControllerScreen(BlockPos pos, double speed) {
        this.pos = pos;
        this.speed = speed;
    }

    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.SPEED.getComponent(), Util::tryParseDoubleFilter)
                .setValue(String.format("%.2f", speed)); // iFields[0]
    }



    @Override
    public void register() {

        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING)
                .withDouble(Util.tryParseDouble(iFields.get(0).getValue()))
                .build();

        AllPackets.getChannel().sendToServer(p);
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.PROPELLER_CONTROLLER.asStack();
    }
}
