package com.verr1.controlcraft.content.blocks.joints;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.generators.ModelFile;

import static com.simibubi.create.foundation.data.BlockStateGen.directionalAxisBlock;
import static com.verr1.controlcraft.registry.ControlCraftShapes.FLAT_BASE;

public class RevoluteJointBlock extends AbstractJointBlock implements IBE<RevoluteJointBlockEntity> {
    public static final String ID = "revolute_joint";


    public RevoluteJointBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLIPPED);
    }

    @Override
    public Class<RevoluteJointBlockEntity> getBlockEntityClass() {
        return RevoluteJointBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RevoluteJointBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.REVOLUTE_JOINT_BLOCKENTITY.get();
    }

    public static class RevoluteJointDataGenerator {

        public static <T extends DirectionalAxisKineticBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> generate(){
            return (c, p) -> directionalAxisBlock(c, p, getModelFunc(c, p));
        }

        public static  <T extends Block> NonNullBiFunction<BlockState, Boolean, ModelFile> getModelFunc(DataGenContext<Block, T> context, RegistrateBlockstateProvider prov){
            return
                    (blockState, isVertical) ->
                    {
                        String levelName = blockState.getValue(LEVEL).name().toLowerCase();
                        String vertical = isVertical ? "_vertical" : "_horizontal";
                        String name = context.getName();
                        return prov.models().getExistingFile(prov.modLoc("block/" + name + "/" + levelName + vertical));
                    };
        }

    }

}
