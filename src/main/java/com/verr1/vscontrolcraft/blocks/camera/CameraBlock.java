package com.verr1.vscontrolcraft.blocks.camera;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CameraBlock extends DirectionalBlock implements IBE<CameraBlockEntity> {
    public final static String ID = "camera";

    public CameraBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return AllShapes.LARGE_GEAR.get(p_60555_.getValue(FACING).getAxis());
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide){
            if(!player.isShiftKeyDown()){
                LinkedCameraManager.link(pos);
            }
        }else{
            if(player.isShiftKeyDown()){
                withBlockEntityDo(worldIn, pos, te -> te.displayScreen((ServerPlayer) player));
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isSignalSource(BlockState p_60571_) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return getBlockEntityOptional(blockAccess, pos).map(CameraBlockEntity::getOutputSignal).orElse(0);
    }

    @Override
    public Class<CameraBlockEntity> getBlockEntityClass() {
        return CameraBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CameraBlockEntity> getBlockEntityType() {
        return AllBlockEntities.CAMERA_BLOCKENTITY.get();
    }


}
