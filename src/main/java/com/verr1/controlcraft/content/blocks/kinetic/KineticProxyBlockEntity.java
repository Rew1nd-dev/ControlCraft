package com.verr1.controlcraft.content.blocks.kinetic;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.api.IKineticPeripheral;
import com.verr1.controlcraft.foundation.api.delegate.IKineticDevice;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KineticProxyBlockEntity extends KineticBlockEntity {


    public KineticProxyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public @NotNull Direction getDirection(){
        if(getBlockState().hasProperty(BlockStateProperties.FACING)) return getBlockState().getValue(BlockStateProperties.FACING);
        return Direction.UP;
    }

    private BlockPos getAttachmentPos(){
        return getBlockPos().relative(getDirection());
    }

    private Optional<IKineticPeripheral> getAttached(){
        return BlockEntityGetter.INSTANCE
                .getCachedBlockEntityAt(
                        WorldBlockPos.of(level, getAttachmentPos()).globalPos(),
                        IKineticDevice.class
                ).map(IKineticDevice::peripheral);
    }


    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        getAttached().ifPresent(p -> p.onSpeedChanged(
                new IKineticPeripheral.KineticContext(
                        speed,
                        previousSpeed,
                        sequenceContext
                )
        ));
    }
}
