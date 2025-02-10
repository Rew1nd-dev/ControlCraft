package com.verr1.vscontrolcraft.deprecated;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlockEntity;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class ServoMotorSyncAnimationPacket extends SimplePacketBase {
    private BlockPos pos;
    private double angle;

    public ServoMotorSyncAnimationPacket(BlockPos pos, double angle) {
        this.pos = pos;
        this.angle = angle;
    }

    public ServoMotorSyncAnimationPacket(FriendlyByteBuf buffer) {
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
            if(blockEntity instanceof ServoMotorBlockEntity servo){
                servo.setAnimatedAngle(angle);
            } else if (blockEntity instanceof JointMotorBlockEntity joint) {
                joint.setAnimatedAngle(angle);
            }
        });
        return true;
    }
}
