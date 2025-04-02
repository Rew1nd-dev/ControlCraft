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

public class PropellerControllerScreen extends SimpleSettingScreen {
    private final double speed;

    public PropellerControllerScreen(BlockPos pos, double speed) {
        super(pos);
        this.speed = speed;
    }

    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.SPEED.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.2f", speed)); // iFields[0]
    }



    @Override
    public void register() {

        var p = new BlockBoundServerPacket.builder(getPos(), RegisteredPacketType.SETTING_0)
                .withDouble(ParseUtils.tryParseDouble(iFields.get(0).getValue()))
                .build();

        ControlCraftPackets.getChannel().sendToServer(p);
    }


    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.PROPELLER_CONTROLLER.asStack();
    }
}
