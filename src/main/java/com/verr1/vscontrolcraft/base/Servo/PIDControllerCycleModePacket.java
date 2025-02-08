package com.verr1.vscontrolcraft.base.Servo;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PIDControllerCycleModePacket extends SimplePacketBase {
    private final BlockPos pos;

    public PIDControllerCycleModePacket(BlockPos pos) {
        this.pos = pos;
    }

    public PIDControllerCycleModePacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(pos);
            if(be instanceof AbstractServoMotor servo){
                servo.toggleMode();
            }
        });
        return true;
    }
}
