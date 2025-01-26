package com.verr1.vscontrolcraft.base.Hinge.packets;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IFlippableHinge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class HingeFlipPacket extends SimplePacketBase {
    private final BlockPos pos;

    public HingeFlipPacket(BlockPos pos) {
        this.pos = pos;
    }

    public HingeFlipPacket(FriendlyByteBuf buf){
        this.pos = buf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if(context.getSender() == null)return;
            BlockEntity blockEntity = context.getSender().level().getExistingBlockEntity(pos);
            if(blockEntity instanceof IFlippableHinge hinge){
                hinge.flip();
            }
        });
        return true;
    }
}
