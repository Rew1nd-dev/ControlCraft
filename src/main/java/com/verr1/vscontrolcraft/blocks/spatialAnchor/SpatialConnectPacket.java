package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class SpatialConnectPacket extends SimplePacketBase {
    private final BlockPos anchor_x;
    private final BlockPos anchor_y;

    public SpatialConnectPacket(BlockPos anchor_x, BlockPos anchor_y) {
        this.anchor_x = anchor_x;
        this.anchor_y = anchor_y;
    }

    public SpatialConnectPacket(FriendlyByteBuf buffer) {
        this.anchor_x = buffer.readBlockPos();
        this.anchor_y = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor_x);
        buffer.writeBlockPos(anchor_y);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be_x = context.getSender().level().getExistingBlockEntity(anchor_x);
            if(!(be_x instanceof SpatialAnchorBlockEntity spatial_x))return;
            spatial_x.bruteDirectionalConnectWith(anchor_y, Direction.UP, Direction.UP); // The last 2 params does not matter in this case

        });
        return true;
    }
}
