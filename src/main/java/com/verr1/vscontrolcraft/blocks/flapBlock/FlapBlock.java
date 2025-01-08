package com.verr1.vscontrolcraft.blocks.flapBlock;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.Wing;
import org.valkyrienskies.mod.common.block.WingBlock;

public class FlapBlock extends DirectionalBlock implements WingBlock {

    protected FlapBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Nullable
    @Override
    public Wing getWing(@Nullable Level level, @Nullable BlockPos blockPos, @NotNull BlockState blockState) {
        return null;
    }
}
