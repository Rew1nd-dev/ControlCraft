package com.verr1.vscontrolcraft.base.Servo;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class ServoMotorSettingsPacket extends SimplePacketBase {
    private double p;
    private double d;
    private double i;

    private double angle;
    private BlockPos pos;

    public ServoMotorSettingsPacket(double p, double i, double d, double a, BlockPos pos) {
        this.p = p;
        this.d = d;
        this.i = i;
        this.angle = a;
        this.pos = pos;
    }

    public ServoMotorSettingsPacket(FriendlyByteBuf buf){
        p = buf.readDouble();
        d = buf.readDouble();
        i = buf.readDouble();
        angle = buf.readDouble();
        pos = buf.readBlockPos();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(p);
        buffer.writeDouble(d);
        buffer.writeDouble(i);
        buffer.writeDouble(angle);
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(pos);
            if(be instanceof AbstractServoMotor servo){
                servo.getControllerInfoHolder().setParameter(p, d, i).setTargetAngle(angle);
            }

        });
        return true;
    }
}
