package com.verr1.vscontrolcraft.blocks.propeller;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PropellerSyncAnimationPacket extends SimplePacketBase {
    private double rotationalSpeed;
    private BlockPos pos;


    public PropellerSyncAnimationPacket(BlockPos pos, double speed) {
        this.pos = pos;
        this.rotationalSpeed = speed;
    }

    public PropellerSyncAnimationPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.rotationalSpeed = buffer.readDouble();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeDouble(rotationalSpeed);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().player.level().getExistingBlockEntity(pos);
            if(blockEntity instanceof PropellerBlockEntity propellerBlockEntity){
                propellerBlockEntity.setVisualRotationalSpeed(rotationalSpeed);
            }
        });
        return true;
    }

}
