package com.verr1.controlcraft.content.blocks.joints;

import com.simibubi.create.foundation.block.IBE;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.function.Function;

public class FreeJointBlock extends AbstractJointBlock implements
        IBE<FreeJointBlockEntity>
{

    public static final String ID = "sphere_hinge";

    public FreeJointBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<FreeJointBlockEntity> getBlockEntityClass() {
        return FreeJointBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FreeJointBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.SPHERE_HINGE_BLOCKENTITY.get();
    }

    public static class DirectionalAdjustableHingeDataGenerator {

        public static  <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> generate(){
            return (c, p) -> p.directionalBlock(c.get(), getModelFunc(c, p));
        }

        public static  <T extends Block> Function<BlockState, ModelFile> getModelFunc(DataGenContext<Block, T> context, RegistrateBlockstateProvider prov){
            return
                    blockState ->
                    {
                        String levelName = blockState.getValue(LEVEL).name().toLowerCase();
                        String name = context.getName();
                        return prov.models().getExistingFile(prov.modLoc("block/" + name + "/" + levelName));
                    };
        }
    }
}
