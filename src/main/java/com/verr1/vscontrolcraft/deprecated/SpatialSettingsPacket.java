package com.verr1.vscontrolcraft.deprecated;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class SpatialSettingsPacket extends SimplePacketBase {
    private final BlockPos pos;
    private final double offset;
    private final long protocol;
    private final boolean isRunning;
    private final boolean isStatic;

    public SpatialSettingsPacket(BlockPos pos, double offset, long protocol, boolean isRunning, boolean isStatic) {
        this.pos = pos;
        this.offset = offset;
        this.protocol = protocol;
        this.isRunning = isRunning;
        this.isStatic = isStatic;
    }

    public SpatialSettingsPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        offset = buffer.readDouble();
        protocol = buffer.readLong();
        isRunning = buffer.readBoolean();
        isStatic = buffer.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeDouble(offset);
        buffer.writeLong(protocol);
        buffer.writeBoolean(isRunning);
        buffer.writeBoolean(isStatic);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(pos);
            if(be instanceof SpatialAnchorBlockEntity spatial){
                spatial.setAnchorOffset(offset);
                spatial.setProtocol(protocol);
                spatial.setRunning(isRunning);
                spatial.setStatic(isStatic);
            }

        });
        return true;
    }
}
