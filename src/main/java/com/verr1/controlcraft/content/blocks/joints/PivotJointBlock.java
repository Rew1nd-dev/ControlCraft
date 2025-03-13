package com.verr1.controlcraft.content.blocks.joints;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PivotJointBlock extends AbstractJointBlock implements IBE<PivotJointBlockEntity>

{
    public static final String ID = "pivot";

    public PivotJointBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<PivotJointBlockEntity> getBlockEntityClass() {
        return PivotJointBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PivotJointBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.PIVOT_JOINT_BLOCKENTITY.get();
    }
}
