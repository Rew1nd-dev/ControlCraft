package com.verr1.vscontrolcraft.blocks.spinalyzer;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class SpinalyzerLinkPacket extends SimplePacketBase {
    BlockPos receiverPos;
    BlockPos transmitterPos;

    public SpinalyzerLinkPacket(BlockPos receiver, BlockPos transmitter){
        receiverPos = receiver;
        transmitterPos = transmitter;
    }

    public SpinalyzerLinkPacket(FriendlyByteBuf buf){
        receiverPos = buf.readBlockPos();
        transmitterPos = buf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(receiverPos);
        buffer.writeBlockPos(transmitterPos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(
            () ->
            {
                ServerPlayer player = context.getSender();
                ServerLevel level = (ServerLevel) player.level();
                BlockEntity receiver = level.getExistingBlockEntity(receiverPos);
                BlockEntity transmitter = level.getExistingBlockEntity(transmitterPos);
                if(receiver instanceof SpinalyzerBlockEntity && transmitter instanceof SpinalyzerBlockEntity){
                    ((SpinalyzerBlockEntity) receiver).pairWith((SpinalyzerBlockEntity) transmitter);
                }

            }
        );
        return true;
    }
}
