package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.IntervalExecutor.IntervalExecutor;
import com.verr1.vscontrolcraft.base.Schedules.FaceAlignmentSchedule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class SimpleAssemblePacket extends SimplePacketBase {
    BlockPos assemPos;
    BlockPos servoPos;
    Direction assemDir;
    Direction servoDir;


    public SimpleAssemblePacket(BlockPos assemPos, BlockPos servoPos, Direction assemDir, Direction servoDir) {
        this.assemPos = assemPos;
        this.servoPos = servoPos;
        this.assemDir = assemDir;
        this.servoDir = servoDir;
    }

    public SimpleAssemblePacket(FriendlyByteBuf buf){
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
            FaceAlignmentSchedule task =
                    new FaceAlignmentSchedule
                            .builder()
                            .basic(
                                servoPos,
                                servoDir,
                                assemPos,
                                assemDir,
                                (ServerLevel) context.getSender().level(),
                                10
                            )
                            .withExpiredTask(() -> {
                                BlockEntity be = context.getSender().level().getExistingBlockEntity(servoPos);
                                if(be instanceof ServoMotorBlockEntity servo){
                                    servo.bruteDirectionalConnectWith(assemPos, assemDir, Direction.UP);
                                }
                            })
                            .build();
            IntervalExecutor.executeOnSchedule(task);
        });
        return true;
    }
}
