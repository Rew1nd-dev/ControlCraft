package com.verr1.vscontrolcraft.base.Hinge.packets;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IAdjustableHinge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class HingeAdjustLevelPacket extends SimplePacketBase {
    private BlockPos hingePos;

    public HingeAdjustLevelPacket(BlockPos hingePos) {
        this.hingePos = hingePos;
    }

    public HingeAdjustLevelPacket(FriendlyByteBuf buffer) {
        this.hingePos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(hingePos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(hingePos);
            if(be instanceof IAdjustableHinge hinge){
                hinge.adjust();

            }

        });
        return true;
    }
}
