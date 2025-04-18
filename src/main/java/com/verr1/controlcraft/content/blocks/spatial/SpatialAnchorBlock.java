package com.verr1.controlcraft.content.blocks.spatial;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.verr1.controlcraft.content.gui.factory.GenericUIFactory;
import com.verr1.controlcraft.foundation.api.common.ISignalAcceptor;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.function.BiFunction;

import static com.simibubi.create.foundation.data.BlockStateGen.directionalAxisBlock;

public class SpatialAnchorBlock extends DirectionalAxisKineticBlock implements
        IBE<SpatialAnchorBlockEntity>, IWrenchable, ISignalAcceptor
{

    public static final String ID = "spatial_anchor";

    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");

    public SpatialAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLIPPED);
    }

    @OnlyIn(Dist.CLIENT)
    protected void displayScreen(BlockPos pos){
        ScreenOpener.open(GenericUIFactory.createSpatialAnchorScreen(pos));
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving)  {
        ISignalAcceptor.super.onNeighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getClickedFace() != state.getValue(FACING))return super.onWrenched(state, context);
        if(state.getValue(FLIPPED)){
            super.onWrenched(state, context);
        }
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), SpatialAnchorBlockEntity::flip);
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(     worldIn.isClientSide
                && handIn == InteractionHand.MAIN_HAND
                && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                && !player.isShiftKeyDown()
        ) {
            displayScreen(pos);
            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public Class<SpatialAnchorBlockEntity> getBlockEntityClass() {
        return SpatialAnchorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SpatialAnchorBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.SPATIAL_ANCHOR_BLOCKENTITY.get();
    }

    public static class SpatialAnchorDataGenerator {
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
}
