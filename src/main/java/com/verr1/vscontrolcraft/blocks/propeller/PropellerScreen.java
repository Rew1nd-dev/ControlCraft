package com.verr1.vscontrolcraft.blocks.propeller;

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
        addNumericFieldWithLabel(ExposedFieldType.TORQUE_RATIO.getComponent(), Util::tryParseDoubleFilter).setValue(String.format("%.1f", torque_ratio));
        addNumericFieldWithLabel(ExposedFieldType.THRUST_RATIO.getComponent(), Util::tryParseDoubleFilter).setValue(String.format("%.1f", thrust_ratio));
    }

    @Override
    public void init() {
        super.init();
        redstoneSettings.visible = false;
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
        return AllBlocks.PROPELLER_BLOCK.asStack();
    }
}

