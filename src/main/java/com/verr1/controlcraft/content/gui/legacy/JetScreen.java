package com.verr1.controlcraft.content.gui.legacy;

import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class JetScreen extends SimpleSettingScreen {

    private final double thrust;
    private final double horizontalTilt;
    private final double verticalTilt;

    public JetScreen(double thrust, double horizontalTilt, double verticalTilt, BlockPos pos) {
        super(pos);
        this.thrust = thrust;
        this.horizontalTilt = horizontalTilt;
        this.verticalTilt = verticalTilt;
    }

    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.THRUST.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.1f", thrust)); // iFields[0]
        addNumericFieldWithLabel(ExposedFieldType.HORIZONTAL_TILT.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.2f", horizontalTilt)); // iFields[1]
        addNumericFieldWithLabel(ExposedFieldType.VERTICAL_TILT.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.2f", verticalTilt)); // iFields[2]

    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(getPos(), RegisteredPacketType.SETTING_0)
                .withDouble(ParseUtils.tryParseDouble(iFields.get(0).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(1).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(2).getValue())) // t h v
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        onClose();
    }



    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.JET_BLOCK.asStack();
    }
}
