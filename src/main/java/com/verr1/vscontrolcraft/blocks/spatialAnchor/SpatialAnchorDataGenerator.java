package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.verr1.vscontrolcraft.blocks.sphericalHinge.SphericalHingeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.function.BiFunction;

import static com.simibubi.create.foundation.data.BlockStateGen.directionalAxisBlock;

public class SpatialAnchorDataGenerator {
    public static <T extends DirectionalAxisKineticBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> generate(){
        return
                (c, p) -> directionalAxisBlock(c, p, modelFunc(c, p));
    }

    private static <T extends DirectionalAxisKineticBlock> BiFunction<BlockState, Boolean, ModelFile> modelFunc(DataGenContext<Block, T> c, RegistrateBlockstateProvider p){
        return (state, vertical) -> {
            boolean flipped = state.getValue(SpatialAnchorBlock.FLIPPED);
            String verticalFix = vertical ? "vertical" : "horizontal";
            String flippedFix = flipped ? "_flipped" : "";
            String name = c.getName();
            return p.models().getExistingFile(p.modLoc("block/" + name + "/" + verticalFix + flippedFix));
        };
    }

}
