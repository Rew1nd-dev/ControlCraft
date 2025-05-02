package com.verr1.controlcraft.foundation.network.packets.specific;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.controlcraft.content.blocks.NetworkBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

public class SyncBlockEntityClientPacket extends SimplePacketBase {
    private final BlockPos pos;
    private final CompoundTag tag;

    public SyncBlockEntityClientPacket(BlockPos pos, CompoundTag tag) {
        this.tag = tag;
        this.pos = pos;
    }

    public SyncBlockEntityClientPacket(FriendlyByteBuf buf){
        tag = buf.readNbt();
        pos = buf.readBlockPos();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeNbt(tag);
        buffer.writeBlockPos(pos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(()->
                Optional
                        .ofNullable(Minecraft.getInstance().level)
                        .map(level -> level.getBlockEntity(pos))
                        .filter(NetworkBlockEntity.class::isInstance)
                        .map(NetworkBlockEntity.class::cast)
                        .ifPresent(syncable -> syncable.receiveSync(tag, context.getSender())));
        return true;
    }
}
