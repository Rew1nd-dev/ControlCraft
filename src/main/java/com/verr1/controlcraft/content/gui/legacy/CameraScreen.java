package com.verr1.controlcraft.content.gui.legacy;

import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CameraScreen extends SimpleSettingScreen {
    private final boolean isActive;

    public CameraScreen(BlockPos pos, boolean isActive) {
        super(pos);
        this.isActive = isActive;
    }

    @Override
    public void startWindow() {
        addBooleanFieldWithLabel(ExposedFieldType.IS_SENSOR.asComponent())
                .setSelected(isActive);
    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(getPos(), RegisteredPacketType.SETTING_1)
                .withBoolean(bFields.get(0).selected())
                .build();

        ControlCraftPackets.getChannel().sendToServer(p);
        onClose();
    }



    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.CAMERA_BLOCK.asStack();
    }
}
