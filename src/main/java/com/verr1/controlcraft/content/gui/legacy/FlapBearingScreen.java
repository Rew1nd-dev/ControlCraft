package com.verr1.controlcraft.content.gui.legacy;

import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FlapBearingScreen extends SimpleSettingScreen {
    private final double angle;

    public FlapBearingScreen(BlockPos pos, double angle) {
        super(pos);
        this.angle = angle;
    }


    @Override
    public void startWindow() {
        addNumericFieldWithLabel(Component.translatable("vscontrolcraft.screen.labels.field.degree"), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.2f", angle));
    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(getPos(), RegisteredPacketType.SETTING_0)
                .withDouble(ParseUtils.tryParseDouble(iFields.get(0).getValue()))
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        onClose();
    }


    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.WING_CONTROLLER_BLOCK.asStack();
    }
}
