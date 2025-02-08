package com.verr1.vscontrolcraft.blocks.spinalyzer;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import static com.verr1.vscontrolcraft.registry.AllShapes.HALF_BOX_BASE;


public class SpinalyzerBlock extends DirectionalBlock implements IBE<SpinalyzerBlockEntity>, IWrenchable {
    public static final String ID = "spinalyzer";


    public SpinalyzerBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace(); // 方块被点击的面


        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return HALF_BOX_BASE.get(state.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide){
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                // ScreenOpener.open(new TestScreen());
            });
        }
        return InteractionResult.PASS;
    }

    @Override
    public Class<SpinalyzerBlockEntity> getBlockEntityClass() {
        return SpinalyzerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SpinalyzerBlockEntity> getBlockEntityType() {
        return AllBlockEntities.SPINALYZER_BLOCKENTITY.get();
    }


}
