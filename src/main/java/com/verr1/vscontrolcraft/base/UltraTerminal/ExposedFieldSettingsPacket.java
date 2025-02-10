package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class ExposedFieldSettingsPacket extends SimplePacketBase {

    private BlockPos pos;
    private ExposedFieldType type;
    private double min;
    private double max;

    public ExposedFieldSettingsPacket(BlockPos pos, ExposedFieldType type, double min, double max) {
        this.pos = pos;
        this.type = type;
        this.min = min;
        this.max = max;
    }

    public ExposedFieldSettingsPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        type = buffer.readEnum(ExposedFieldType.class);
        min = buffer.readDouble();
        max = buffer.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {

        buffer.writeBlockPos(pos);
        buffer.writeEnum(type);
        buffer.writeDouble(min);
        buffer.writeDouble(max);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(()->{
            BlockEntity be = context.getSender().level().getExistingBlockEntity(pos);
            if(be instanceof ITerminalDevice device){
                device.setExposedField(type, min, max);
            }
        });
        return true;
    }
}
