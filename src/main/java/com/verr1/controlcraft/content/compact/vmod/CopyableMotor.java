package com.verr1.controlcraft.content.compact.vmod;

import com.verr1.controlcraft.content.blocks.motor.AbstractMotor;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.spaceeye.valkyrien_ship_schematics.interfaces.ICopyableBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.List;
import java.util.Map;

public interface CopyableMotor extends ICopyableBlock {

    @Nullable
    @Override
    default CompoundTag onCopy(
            @NotNull ServerLevel serverLevel,
            @NotNull BlockPos blockPos,
            @NotNull BlockState blockState,
            @Nullable BlockEntity blockEntity,
            @NotNull List<? extends ServerShip> list
    ) {
        if(!(blockEntity instanceof AbstractMotor motor)) return null;
        return VSchematicCompactCenter.PreWriteMotorVModCompact(motor);
    }

    @Override
    default void onPaste(
            @NotNull ServerLevel serverLevel,
            @NotNull BlockPos blockPos,
            @NotNull BlockState blockState,
            @NotNull Map<Long, Long> map,
            @Nullable CompoundTag compoundTag,
            @NotNull Function2<
                    ? super Boolean,
                    ? super Function1<
                            ? super CompoundTag,
                            ? extends CompoundTag
                            >,
                    Unit> function2,
            @NotNull Function1<
                    ? super Function1<
                            ? super BlockEntity
                            , Unit>
                    , Unit> function1
    ){
        function2.invoke(true, null);
        VSchematicCompactCenter.PreMotorReadVModCompact(serverLevel, map, compoundTag);
    }

    @Override
    default void onPasteNoTag(@NotNull ServerLevel serverLevel, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull Map<Long, Long> map){};
}
