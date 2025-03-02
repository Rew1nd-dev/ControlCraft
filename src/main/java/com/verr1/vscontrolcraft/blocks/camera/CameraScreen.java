package com.verr1.vscontrolcraft.blocks.camera;

import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CameraScreen extends SimpleSettingScreen {
    private final BlockPos pos;
    private final boolean isActive;

    public CameraScreen(BlockPos pos, boolean isActive) {
        this.pos = pos;
        this.isActive = isActive;
    }

    @Override
    public void startWindow() {
        addBooleanFieldWithLabel(ExposedFieldType.IS_SENSOR.getComponent())
                .setSelected(isActive);
    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING_1)
                .withBoolean(bFields.get(0).selected())
                .build();

        AllPackets.getChannel().sendToServer(p);
        onClose();
    }

    @Override
    public @NotNull BlockPos getPos() {
        return pos;
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.CAMERA_BLOCK.asStack();
    }
}
