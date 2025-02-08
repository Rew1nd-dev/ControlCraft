package com.verr1.vscontrolcraft.blocks.wingController;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.verr1.vscontrolcraft.registry.AllShapes.HALF_BOX_BASE;

public class WingControllerBlock extends BearingBlock implements
        IBE<WingControllerBlockEntity>
{
    public static final String ID = "wing_controller";

    public WingControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return null;
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if(hit.getDirection() != state.getValue(WingControllerBlock.FACING))
            return InteractionResult.PASS;
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
    public Class<WingControllerBlockEntity> getBlockEntityClass() {
        return WingControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WingControllerBlockEntity> getBlockEntityType() {
        return AllBlockEntities.WING_CONTROLLER_BLOCKENTITY.get();
    }
}
