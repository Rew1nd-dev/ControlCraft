package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.base.Servo.PID;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerOpenScreenPacket;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.*;

import java.lang.Math;

public class ServoMotorBlockEntity extends AbstractServoMotor{

    private boolean assembleNextTick = false;


    public ServoMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        lazyTickRate = 0;
    }



    public Vector3d getServoDirectionJOML(){
        return Util.Vec3itoVector3d(getBlockState().getValue(ServoMotorBlock.FACING).getNormal()) ;
    }

    @Override
    public BlockPos getAssembleBlockPos() {
        return getBlockPos().relative(getBlockState().getValue(ServoMotorBlock.FACING));
    }

    @Override
    public Vector3d getAssembleBlockPosJOML() {
        Vector3d center = Util.Vec3toVector3d(getAssembleBlockPos().getCenter());
        Vector3d dir = getServoDirectionJOML();
        return center.fma(0.0, dir);
    }

    public Direction getServoDirection(){
        return getBlockState().getValue(ServoMotorBlock.FACING);
    }

    public void setAssembleNextTick(){
        assembleNextTick = true;
    }


    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        double createInput2Omega = speed / 60 * 2 * Math.PI;
        if(!isAdjustingAngle()) getControllerInfoHolder().setTarget(createInput2Omega);
    }

    @Override
    public void tick() {
        super.tick();
        if(assembleNextTick){
            assemble();
            assembleNextTick = false;
        }

        syncCompanionAttachInducer();
        if(level.isClientSide){
            tickAnimation();
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level.isClientSide)return;
        syncClient();
    }

    protected void displayScreen(ServerPlayer player){

        double t = getControllerInfoHolder().getTarget();
        double v = getControllerInfoHolder().getValue();
        double o = getOffset();
        boolean m = isAdjustingAngle();
        boolean c = isCheatMode();
        PID pidParams = getControllerInfoHolder().getPIDParams();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN)
                .withDouble(t)
                .withDouble(v)
                .withDouble(pidParams.p())
                .withDouble(pidParams.i())
                .withDouble(pidParams.d())
                .withDouble(o)
                .withBoolean(m)
                .withBoolean(c)
                .build();

        AllPackets.sendToPlayer(p, player);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        super.handleClient(context, packet);
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN){
            double t = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double p = packet.getDoubles().get(2);
            double i = packet.getDoubles().get(3);
            double d = packet.getDoubles().get(4);
            double o = packet.getDoubles().get(5);
            boolean m = packet.getBooleans().get(0);
            boolean c = packet.getBooleans().get(1);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new ServoMotorScreen(getBlockPos(), p, i, d, v, t, o, m, c)));
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        super.handleServer(context, packet);
        if(packet.getType() == BlockBoundPacketType.TOGGLE){
            setCheatMode(!isCheatMode());
        }
    }

}
