package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.Servo.IPIDController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class SpatialSettingsPacket extends SimplePacketBase {
    private final BlockPos pos;
    private final double offset;
    private final long protocol;

    public SpatialSettingsPacket(BlockPos pos, double offset, long protocol) {
        this.pos = pos;
        this.offset = offset;
        this.protocol = protocol;
    }

    public SpatialSettingsPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        offset = buffer.readDouble();
        protocol = buffer.readLong();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeDouble(offset);
        buffer.writeLong(protocol);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(pos);
            if(be instanceof SpatialAnchorBlockEntity spatial){
                spatial.setAnchorOffset(offset);
                spatial.setProtocol(protocol);
            }

        });
        return true;
    }
}
