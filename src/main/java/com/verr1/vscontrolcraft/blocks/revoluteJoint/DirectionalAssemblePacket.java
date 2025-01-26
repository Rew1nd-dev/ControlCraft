package com.verr1.vscontrolcraft.blocks.revoluteJoint;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.IntervalExecutor.IntervalExecutor;
import com.verr1.vscontrolcraft.base.Schedules.FaceAlignmentSchedule;
import com.verr1.vscontrolcraft.base.Servo.ICanBruteDirectionalConnect;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlockEntity;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class DirectionalAssemblePacket extends SimplePacketBase {
    BlockPos assemPos;
    BlockPos servoPos;
    Direction assemAlign;
    Direction servoAlign;
    Direction assemForward;
    Direction servoForward;

    public DirectionalAssemblePacket(
            BlockPos assemPos,
            BlockPos servoPos,
            Direction assemAlign,
            Direction servoAlign,
            Direction assemForward,
            Direction servoForward) {
        this.assemPos = assemPos;
        this.servoPos = servoPos;
        this.assemAlign = assemAlign;
        this.servoAlign = servoAlign;
        this.assemForward = assemForward;
        this.servoForward = servoForward;
    }

    public DirectionalAssemblePacket(FriendlyByteBuf buf) {
        assemPos = buf.readBlockPos();
        servoPos = buf.readBlockPos();
        assemAlign = buf.readEnum(Direction.class);
        servoAlign = buf.readEnum(Direction.class);
        assemForward = buf.readEnum(Direction.class);
        servoForward = buf.readEnum(Direction.class);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(assemPos);
        buffer.writeBlockPos(servoPos);
        buffer.writeEnum(assemAlign);
        buffer.writeEnum(servoAlign);
        buffer.writeEnum(assemForward);
        buffer.writeEnum(servoForward);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            FaceAlignmentSchedule task =
                new FaceAlignmentSchedule
                        .builder()
                        .basic(
                            servoPos,
                            servoAlign,
                            assemPos,
                            assemAlign,
                            (ServerLevel) context.getSender().level(),
                            10
                        )
                        .withExpiredTask(() -> {
                            BlockEntity be = context.getSender().level().getExistingBlockEntity(servoPos);
                            if(be instanceof ICanBruteDirectionalConnect joint){
                                joint.bruteDirectionalConnectWith(assemPos, assemForward, assemAlign);
                            }
                        })
                        .withOverriddenAlignExtra(
                                VSMathUtils.rotationToAlign(
                                        servoAlign,
                                        servoForward,
                                        assemAlign,
                                        assemForward
                                )
                        )
                        .build();
            IntervalExecutor.executeOnSchedule(task);
        });
        return true;
    }
}
