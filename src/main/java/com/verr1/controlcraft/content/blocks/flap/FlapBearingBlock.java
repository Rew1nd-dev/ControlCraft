package com.verr1.controlcraft.content.blocks.flap;

import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.gui.factory.GenericUIFactory;
import com.verr1.controlcraft.foundation.api.ISignalAcceptor;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FlapBearingBlock extends BearingBlock implements
        IBE<FlapBearingBlockEntity>, ISignalAcceptor
{
    public static final String ID = "wing_controller";

    public FlapBearingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return null;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                  boolean isMoving)  {
        ISignalAcceptor.super.onNeighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @OnlyIn(Dist.CLIENT)
    public void displayScreen(BlockPos p){
        ScreenOpener.open(GenericUIFactory.createFlapBearingScreen(p));
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if(hit.getDirection() != state.getValue(FlapBearingBlock.FACING)){
            if(     worldIn.isClientSide
                    && handIn == InteractionHand.MAIN_HAND
                    && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                    && !player.isShiftKeyDown()
            ){
                displayScreen(pos);
            }
            return InteractionResult.SUCCESS;
        }
        if (!player.mayBuild())
            return InteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return InteractionResult.FAIL;
        if (player.getItemInHand(handIn)
                .isEmpty()) {
            if (worldIn.isClientSide)
                return InteractionResult.SUCCESS;
            withBlockEntityDo(worldIn, pos, be -> {
                if(!be.running){
                    be.assemble();
                }else{
                    be.disassemble();
                }

            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    @Override
    public Class<FlapBearingBlockEntity> getBlockEntityClass() {
        return FlapBearingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FlapBearingBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.WING_CONTROLLER_BLOCKENTITY.get();
    }
}
