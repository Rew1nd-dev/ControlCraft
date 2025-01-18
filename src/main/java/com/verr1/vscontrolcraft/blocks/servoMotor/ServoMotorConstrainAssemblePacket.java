package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.IntervalExecutor.IntervalExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.CompletableFuture;

public class ServoMotorConstrainAssemblePacket extends SimplePacketBase {
    BlockPos assemPos;
    BlockPos servoPos;
    Direction assemDir;
    Direction servoDir;


    public ServoMotorConstrainAssemblePacket(BlockPos assemPos, BlockPos servoPos, Direction assemDir, Direction servoDir) {
        this.assemPos = assemPos;
        this.servoPos = servoPos;
        this.assemDir = assemDir;
        this.servoDir = servoDir;
    }

    public ServoMotorConstrainAssemblePacket(FriendlyByteBuf buf){
        assemPos = buf.readBlockPos();
        servoPos = buf.readBlockPos();
        assemDir = buf.readEnum(Direction.class);
        servoDir = buf.readEnum(Direction.class);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(assemPos);
        buffer.writeBlockPos(servoPos);
        buffer.writeEnum(assemDir);
        buffer.writeEnum(servoDir);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServoConstrainAssembleSchedule task = new ServoConstrainAssembleSchedule(
                    servoPos,
                    servoDir,
                    assemPos,
                    assemDir,
                    (ServerLevel) context.getSender().level(),
                    10
            );
            IntervalExecutor.executeOnSchedule(task);
        });
        return true;
    }
}
