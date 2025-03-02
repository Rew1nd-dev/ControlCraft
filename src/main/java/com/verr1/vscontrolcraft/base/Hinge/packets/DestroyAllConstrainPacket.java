package com.verr1.vscontrolcraft.base.Hinge.packets;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.Constrain.ConstrainCenter;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IConstrainHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class DestroyAllConstrainPacket extends SimplePacketBase {
    private final BlockPos pos;

    public DestroyAllConstrainPacket(BlockPos pos) {
        this.pos = pos;
    }

    public DestroyAllConstrainPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if(context.getSender() == null)return;
            Level level = context.getSender().level();
            if(!(level instanceof ServerLevel serverLevel))return;
            ConstrainCenter.destroyAllConstrains(serverLevel, pos);
        });
        return true;
    }
}
