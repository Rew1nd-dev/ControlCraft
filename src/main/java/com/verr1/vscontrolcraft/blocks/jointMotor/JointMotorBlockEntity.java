package com.verr1.vscontrolcraft.blocks.jointMotor;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.base.Servo.PID;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.*;

import java.lang.Math;

public class JointMotorBlockEntity extends AbstractServoMotor
{
    private boolean assembleNextTick = false;



    private boolean reverseCreateInput = false;

    public JointMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        lazyTickRate = 0;
    }

    public boolean isReverseCreateInput() {
        return reverseCreateInput;
    }

    public void setReverseCreateInput(boolean reversed){
        reverseCreateInput = reversed;
        setTargetFromCreate();
        setChanged();
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        setTargetFromCreate();
    }

    public void setTargetFromCreate(){
        double createInput2Omega = speed / 60 * 2 * Math.PI;
        double sign = reverseCreateInput ? -1 : 1;
        if(!isAdjustingAngle()) {
            getControllerInfoHolder().setTarget(createInput2Omega * sign);
        }
    }

    public Direction getServoDirection(){

        Direction facing = getBlockState().getValue(JointMotorBlock.FACING);
        Boolean align = getBlockState().getValue(JointMotorBlock.AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

    public Vector3d getServoDirectionJOML(){
        return Util.Vec3itoVector3d(getServoDirection().getNormal());
    }

    @Override
    public BlockPos getAssembleBlockPos() {
        return getBlockPos().relative(getFacingDirection(), 1);
    }

    @Override
    public Vector3d getAssembleBlockPosJOML() {
        Vector3d p = Util.Vec3toVector3d(getBlockPos().getCenter());
        Vector3d dir = getFacingDirectionJOML();
        return p.fma(1.0, dir);
    }

    public Direction getFacingDirection(){
        return getBlockState().getValue(JointMotorBlock.FACING);
    }

    public Vector3d getFacingDirectionJOML(){
        return Util.Vec3itoVector3d(getFacingDirection().getNormal());
    }

    public void setAssembleNextTick(){
        assembleNextTick = true;
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
        boolean m = isAdjustingAngle();
        boolean c = isCheatMode();
        PID pidParams = getControllerInfoHolder().getPIDParams();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN_0)
                .withDouble(t)
                .withDouble(v)
                .withDouble(pidParams.p())
                .withDouble(pidParams.i())
                .withDouble(pidParams.d())
                .withBoolean(m)
                .withBoolean(c)
                .build();

        AllPackets.sendToPlayer(p, player);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        super.handleClient(context, packet);
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN_0){
            double t = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double p = packet.getDoubles().get(2);
            double i = packet.getDoubles().get(3);
            double d = packet.getDoubles().get(4);
            boolean m = packet.getBooleans().get(0);
            boolean c = packet.getBooleans().get(1);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new JointMotorScreen(getBlockPos(), p, i, d, v, t, m, c)));
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        super.handleServer(context, packet);
        if(packet.getType() == BlockBoundPacketType.TOGGLE_0){
            setCheatMode(!isCheatMode());
        }
        if(packet.getType() == BlockBoundPacketType.TOGGLE_1){
            setReverseCreateInput(!isReverseCreateInput());
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if(clientPacket)return;
        tag.putBoolean("reverseCreate", reverseCreateInput);
    }


    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(clientPacket)return;
        reverseCreateInput = tag.getBoolean("reverseCreate");
    }
}
