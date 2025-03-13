package com.verr1.controlcraft.content.gui;

import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JetScreen extends SimpleSettingScreen {

    private final double thrust;
    private final double horizontalTilt;
    private final double verticalTilt;
    private final BlockPos pos;

    public JetScreen(double thrust, double horizontalTilt, double verticalTilt, BlockPos pos) {
        this.thrust = thrust;
        this.horizontalTilt = horizontalTilt;
        this.verticalTilt = verticalTilt;
        this.pos = pos;
    }

    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.THRUST.getComponent(), ParseUtils::tryParseDoubleFilter)
                .setValue(String.format("%.1f", thrust)); // iFields[0]
        addNumericFieldWithLabel(ExposedFieldType.HORIZONTAL_TILT.getComponent(), ParseUtils::tryParseDoubleFilter)
                .setValue(String.format("%.2f", horizontalTilt)); // iFields[1]
        addNumericFieldWithLabel(ExposedFieldType.VERTICAL_TILT.getComponent(), ParseUtils::tryParseDoubleFilter)
                .setValue(String.format("%.2f", verticalTilt)); // iFields[2]

    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.SETTING_0)
                .withDouble(ParseUtils.tryParseDouble(iFields.get(0).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(1).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(2).getValue())) // t h v
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        onClose();
    }

    @Override
    public @NotNull BlockPos getPos() {
        return pos;
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.JET_BLOCK.asStack();
    }
}
