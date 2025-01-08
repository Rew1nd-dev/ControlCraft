package com.verr1.vscontrolcraft.blocks.propeller;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.base.IPacketHandle;
import com.verr1.vscontrolcraft.base.ISyncable;
import com.verr1.vscontrolcraft.base.SyncAnimationPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class PropellerBlockEntity extends SmartBlockEntity implements ISyncable{
    public double ThrustRatio = 1000;
    public double TorqueRatio = 1000;
    public double rotationalSpeed = 5;

    public LerpedFloat angle;
    public float targetAngle = 0;

    public PropellerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        angle = LerpedFloat.angular();
    }

    public double getThrustRatio() {
        return ThrustRatio;
    }

    public double getTorqueRatio(){
        return TorqueRatio;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    public void setVisualRotationalSpeed(double speed){
        rotationalSpeed = speed;
    }



    @Override
    public void tick() {
        super.tick();
        targetAngle = Util.angleReset(targetAngle + (float) (rotationalSpeed * 0.05 * 0.175));
        if (level.isClientSide) {
            tickAnimation();
        }
    }

    @Override
    public void lazyTick() {
        if(level.isClientSide)return;
        syncClient();
    }

    @OnlyIn(Dist.CLIENT)
    private void tickAnimation(){
        angle.chase(targetAngle, 0.1, LerpedFloat.Chaser.EXP);
        angle.tickChaser();
    }




    @Override
    public void syncClient() {
        if(!level.isClientSide){
            var p = new SyncAnimationPacket<>(this, new PropellerAnimationDataHandler(rotationalSpeed), PropellerBlockEntity.class);
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    public static class PropellerAnimationDataHandler implements IPacketHandle<PropellerBlockEntity>{
        private double rotationalSpeed;

        public PropellerAnimationDataHandler(){
            rotationalSpeed = 0;
        }

        public PropellerAnimationDataHandler(double speed){
            rotationalSpeed = speed;
        }

        @Override
        public void readBuffer(FriendlyByteBuf buffer) {
            rotationalSpeed = buffer.readDouble();
        }

        @Override
        public void writeBuffer(FriendlyByteBuf buffer) {
            buffer.writeDouble(rotationalSpeed);
        }

        @Override
        public void handle(PropellerBlockEntity be) {
            be.setVisualRotationalSpeed(rotationalSpeed);
        }
    }
}
