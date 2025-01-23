package com.verr1.vscontrolcraft.blocks.sphereHinge;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.function.Function;

public class SphericalHingeDataGenerator {

    public static  <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> generate(){
        return (c, p) -> p.directionalBlock(c.get(), getModelFunc(c, p));
    }


    public static  <T extends Block> Function<BlockState, ModelFile> getModelFunc(DataGenContext<Block, T> context, RegistrateBlockstateProvider prov){
        return
                blockState ->
        {
            String levelName = blockState.getValue(SphericalHingeBlock.LEVEL).name().toLowerCase();
            String name = context.getName();
            return prov.models().getExistingFile(prov.modLoc("block/" + name + "/sph_" + levelName));
        };
    }
}
