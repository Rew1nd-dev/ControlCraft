package com.verr1.controlcraft.foundation.network.packets.specific;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.controlcraft.content.blocks.NetworkBlockEntity;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class LazyRequestBlockEntitySyncPacket extends SimplePacketBase {
    private final BlockPos pos;
    private final ArrayList<NetworkKey> requests = new ArrayList<>();

    public LazyRequestBlockEntitySyncPacket(BlockPos pos, List<NetworkKey> requests) {
        this.pos = pos;
        this.requests.addAll(requests);
    }

    public LazyRequestBlockEntitySyncPacket(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            requests.add(NetworkKey.create(buf.readUtf()));
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(requests.size());
        for (NetworkKey request : requests) {
            buffer.writeUtf(request.getSerializedName());
        }
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(()->{
            BlockEntity be = context.getSender().level().getExistingBlockEntity(pos);
            if(be instanceof NetworkBlockEntity obe){
                obe.receiveRequest(requests, context.getSender());
            }

        });

        return true;
    }
}
