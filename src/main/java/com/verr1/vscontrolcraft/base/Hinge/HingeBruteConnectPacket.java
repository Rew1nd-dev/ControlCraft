package com.verr1.vscontrolcraft.base.Hinge;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class HingeBruteConnectPacket extends SimplePacketBase {
    private final BlockPos xHingePos;
    private final BlockPos yHingePos;

    public HingeBruteConnectPacket(BlockPos yHingePos, BlockPos xHingePos) {
        this.yHingePos = yHingePos;
        this.xHingePos = xHingePos;
    }

    public HingeBruteConnectPacket(FriendlyByteBuf buf){
        yHingePos = buf.readBlockPos();
        xHingePos = buf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(yHingePos);
        buffer.writeBlockPos(xHingePos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerLevel level = (ServerLevel) context.getSender().level();
            BlockEntity xBe = level.getBlockEntity(xHingePos);
            if(!(xBe instanceof ICanBruteConnect xHinge))return;
            xHinge.bruteConnectWith(yHingePos);
        });
        return true;
    }
}
