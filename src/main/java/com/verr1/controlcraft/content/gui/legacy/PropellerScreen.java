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

public class PropellerScreen extends SimpleSettingScreen {

    private final double thrust_ratio;
    private final double torque_ratio;

    public PropellerScreen(BlockPos entityPos, double thrust_ratio, double torque_ratio) {
        super(entityPos);
        this.thrust_ratio = thrust_ratio;
        this.torque_ratio = torque_ratio;
    }



    @Override
    public void tick(){
        super.tick();
    }

    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.THRUST_RATIO.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.1f", thrust_ratio));
        addNumericFieldWithLabel(ExposedFieldType.TORQUE_RATIO.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.1f", torque_ratio));
    }

    @Override
    public void init() {
        super.init();
        redstoneSettings.visible = false;
    }

    public void register() {
        var p = new BlockBoundServerPacket.builder(getPos(), RegisteredPacketType.SETTING_0)
                .withDouble(ParseUtils.tryParseDouble(iFields.get(0).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(1).getValue()))
                .build();
        ControlCraftPackets
                .getChannel()
                .sendToServer(p);
        onClose();
    }


    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.PROPELLER_BLOCK.asStack();
    }
}
