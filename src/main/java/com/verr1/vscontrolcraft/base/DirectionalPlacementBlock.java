package com.verr1.vscontrolcraft.base;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionalPlacementBlock extends DirectionalKineticBlock implements IWrenchable {

    public DirectionalPlacementBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return null;
    }
}
