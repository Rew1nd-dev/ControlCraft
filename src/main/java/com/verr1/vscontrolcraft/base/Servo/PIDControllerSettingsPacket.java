package com.verr1.vscontrolcraft.base.Servo;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PIDControllerSettingsPacket extends SimplePacketBase {
    private double p;
    private double d;
    private double i;

    private double value;
    private BlockPos pos;

    public PIDControllerSettingsPacket(double p, double i, double d, double a, BlockPos pos) {
        this.p = p;
        this.d = d;
        this.i = i;
        this.value = a;
        this.pos = pos;
    }

    public PIDControllerSettingsPacket(FriendlyByteBuf buf){
        p = buf.readDouble();
        d = buf.readDouble();
        i = buf.readDouble();
        value = buf.readDouble();
        pos = buf.readBlockPos();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(p);
        buffer.writeDouble(d);
        buffer.writeDouble(i);
        buffer.writeDouble(value);
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(pos);
            if(be instanceof IPIDController controller){
                controller.getControllerInfoHolder().setParameter(p, d, i).setTarget(value);
            }

        });
        return true;
    }
}
