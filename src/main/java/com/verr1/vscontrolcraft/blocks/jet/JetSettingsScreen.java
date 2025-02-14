package com.verr1.vscontrolcraft.blocks.jet;

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

public class JetSettingsScreen extends SimpleSettingScreen {

    private final double thrust;
    private final double horizontalTilt;
    private final double verticalTilt;
    private final BlockPos pos;

    public JetSettingsScreen(double thrust, double horizontalTilt, double verticalTilt, BlockPos pos) {
        this.thrust = thrust;
        this.horizontalTilt = horizontalTilt;
        this.verticalTilt = verticalTilt;
        this.pos = pos;
    }

    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.THRUST.getComponent(), Util::tryParseDoubleFilter)
                .setValue(String.format("%.1f", thrust)); // iFields[0]
        addNumericFieldWithLabel(ExposedFieldType.HORIZONTAL_TILT.getComponent(), Util::tryParseDoubleFilter)
                .setValue(String.format("%.2f", horizontalTilt)); // iFields[1]
        addNumericFieldWithLabel(ExposedFieldType.VERTICAL_TILT.getComponent(), Util::tryParseDoubleFilter)
                .setValue(String.format("%.2f", verticalTilt)); // iFields[2]

    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING_0)
                .withDouble(Util.tryParseDouble(iFields.get(0).getValue()))
                .withDouble(Util.tryParseDouble(iFields.get(1).getValue()))
                .withDouble(Util.tryParseDouble(iFields.get(2).getValue())) // t h v
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
        return AllBlocks.JET_BLOCK.asStack();
    }
}
