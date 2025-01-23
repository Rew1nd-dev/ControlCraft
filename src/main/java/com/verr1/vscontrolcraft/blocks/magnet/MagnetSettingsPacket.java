package com.verr1.vscontrolcraft.blocks.magnet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class MagnetSettingsPacket extends SimplePacketBase {
    private BlockPos pos;
    private double strength;

    public MagnetSettingsPacket(double strength, BlockPos pos) {
        this.pos = pos;
        this.strength = strength;
    }

    public MagnetSettingsPacket(FriendlyByteBuf buffer) {
        this.strength = buffer.readDouble();
        this.pos = buffer.readBlockPos();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(strength);
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(()->{
            BlockEntity be = context.getSender().level().getExistingBlockEntity(pos);
            if(be instanceof MagnetBlockEntity magnet){
                magnet.setStrength(strength);
            }
        });
        return true;
    }
}
