package com.verr1.controlcraft.content.blocks;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SidedTickedBlockEntity extends SmartBlockEntity {
    public SidedTickedBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {

    }


    public void tickServer(){

    }
    public void tickClient(){

    }
    public void tickCommon(){
    }
    public void lazyTickServer(){}
    public void lazyTickClient(){}
    public void lazyTickCommon(){}
    @Override
    public void lazyTick() {
        super.lazyTick();
        lazyTickCommon();
        if(level != null && level.isClientSide)lazyTickClient();
        else lazyTickServer();
    }
    @Override
    public void tick(){
        super.tick();
        tickCommon();
        if(level != null && level.isClientSide)tickClient();
        else tickServer();
    }

}
