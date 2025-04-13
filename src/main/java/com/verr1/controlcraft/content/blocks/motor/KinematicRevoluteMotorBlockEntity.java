package com.verr1.controlcraft.content.blocks.motor;

import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;

public class KinematicRevoluteMotorBlockEntity extends AbstractKinematicMotor {

    public KinematicRevoluteMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Direction getServoDirection() {
        return getBlockState().getValue(DynamicRevoluteMotorBlock.FACING);
    }

    @Override
    public BlockPos getAssembleBlockPos() {
        return getBlockPos().relative(getBlockState().getValue(DynamicRevoluteMotorBlock.FACING));
    }

    @Override
    public Vector3d getRotationCenterPosJOML() {
        Vector3d center = ValkyrienSkies.set(new Vector3d(), getAssembleBlockPos().getCenter());
        return center.add(getSelfOffset());
    }


    public void displayScreen(ServerPlayer player){

        double t = getController().getControlTarget();
        double v = getServoAngle();
        double o = getSelfOffset().get(0);
        double c = getCompliance();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(t)
                .withDouble(v)
                .withDouble(o)
                .withDouble(c)
                .build();

        ControlCraftPackets.sendToPlayer(p, player);
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == RegisteredPacketType.SETTING_0){
            // setOffset(new Vector3d(packet.getDoubles().get(0), 0, 0));
        }
        if(packet.getType() == RegisteredPacketType.SETTING_1){
            getController().setControlTarget(packet.getDoubles().get(0));
            // setOffset(new Vector3d(packet.getDoubles().get(0), 0, 0));
            setCompliance(packet.getDoubles().get(2));
        }
        if(packet.getType() == RegisteredPacketType.TOGGLE_0){
            setTargetMode(getTargetMode() == TargetMode.VELOCITY ? TargetMode.POSITION : TargetMode.VELOCITY);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == RegisteredPacketType.SYNC_0){
            double angle = packet.getDoubles().get(0);
            clientAngle = (float) angle;
        }
    }

}
