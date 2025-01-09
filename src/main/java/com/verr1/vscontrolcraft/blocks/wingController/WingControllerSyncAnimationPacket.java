package com.verr1.vscontrolcraft.blocks.wingController;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class WingControllerSyncAnimationPacket extends SimplePacketBase {
    private double angle;
    private BlockPos pos;


    public WingControllerSyncAnimationPacket(BlockPos pos, double angle) {
        this.pos = pos;
        this.angle = angle;
    }

    public WingControllerSyncAnimationPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.angle = buffer.readDouble();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeDouble(angle);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().player.level().getExistingBlockEntity(pos);
            if(blockEntity instanceof WingControllerBlockEntity propellerBlockEntity){
                propellerBlockEntity.setAngle((float)angle);
            }
        });
        return true;
    }

}
