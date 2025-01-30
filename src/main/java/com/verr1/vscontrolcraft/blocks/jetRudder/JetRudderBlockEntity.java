package com.verr1.vscontrolcraft.blocks.jetRudder;

import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.base.OnShipBlockEntity;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerSyncAnimationPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

public class JetRudderBlockEntity extends OnShipBlockEntity {

    public LerpedFloat animatedHorizontalAngle = LerpedFloat.angular();
    public float targetHorizontalAngle = 0;
    public LerpedFloat animatedVerticalAngle = LerpedFloat.angular();
    public float targetVerticalAngle = 0;


    private double horizontalAngle = 10;
    private double verticalAngle = 10;


    public JetRudderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setAnimatedAngles(float horizontal, float vertical){
        targetHorizontalAngle =(float)VSMathUtils.clamp(horizontal, 15);
        targetVerticalAngle =(float)VSMathUtils.clamp(vertical, 15);
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide) {
            tickAnimation();
        }else{
            syncClient();
        }

    }

    public void syncClient() {
        if(!level.isClientSide){
            var p = new JetRudderSyncAnimationPacket(getBlockPos(), targetHorizontalAngle, targetVerticalAngle);
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickAnimation(){
        animatedHorizontalAngle.chase(targetHorizontalAngle , 0.1, LerpedFloat.Chaser.EXP);
        animatedVerticalAngle.chase(targetVerticalAngle , 0.1, LerpedFloat.Chaser.EXP);
        animatedHorizontalAngle.tickChaser();
        animatedVerticalAngle.tickChaser();
    }


}
