package com.verr1.vscontrolcraft.blocks.revoluteJoint;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.IntervalExecutor.IntervalExecutor;
import com.verr1.vscontrolcraft.base.Schedules.FaceAlignmentSchedule;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlockEntity;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlockEntity;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class RevoluteJointConstrainAssemblePacket extends SimplePacketBase {
    BlockPos assemPos;
    BlockPos servoPos;
    Direction assemAlign;
    Direction servoAlign;
    Direction assemForward;
    Direction servoForward;

    public RevoluteJointConstrainAssemblePacket(
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

    public RevoluteJointConstrainAssemblePacket(FriendlyByteBuf buf) {
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
                new FaceAlignmentSchedule(
                        servoPos,
                        servoAlign,
                        assemPos,
                        assemAlign,
                        (ServerLevel) context.getSender().level(),
                        10
                ).withExpiredTask(() -> {
                    BlockEntity be = context.getSender().level().getExistingBlockEntity(servoPos);
                    if(be instanceof JointMotorBlockEntity joint){
                        joint.bruteDirectionalConnectWith(assemPos, assemForward);
                    }
                }).withExtraQuaternion(
                        VSMathUtils.rotationToAlign(
                                servoAlign,
                                servoForward,
                                assemAlign,
                                assemForward
                        )
                );
            IntervalExecutor.executeOnSchedule(task);
        });
        return true;
    }
}
