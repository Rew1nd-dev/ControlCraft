package com.verr1.controlcraft.content.compact.vmod;

import com.verr1.controlcraft.ControlCraft;
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
import java.util.function.Function;

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
        ControlCraft.LOGGER.info("On Copy Called");
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
            @NotNull Function1<? super Boolean, Unit> delayLoading,
            @NotNull Function1<? super Function1<? super BlockEntity, Unit>, Unit> function11)
    {
        delayLoading.invoke(true);
        VSchematicCompactCenter.PreMotorReadVModCompact(serverLevel, map, compoundTag);
    };


    default void __onPaste(
            @NotNull ServerLevel serverLevel,
            @NotNull BlockPos blockPos,
            @NotNull BlockState blockState,
            @NotNull Map<Long, Long> map,
            @Nullable CompoundTag compoundTag,
            @NotNull Function2<
                    ? super Boolean,
                    ? super Function1<? super CompoundTag, ? extends CompoundTag>,
                    Unit> delayLoading,
            @NotNull Function1<
                    ? super Function1<
                            ? super BlockEntity
                            , Unit>
                    , Unit> function1
    ){
        Function1<CompoundTag, CompoundTag> func = tag -> VSchematicCompactCenter.PreMotorReadVModCompact(serverLevel, map, tag);
        delayLoading.invoke(true, func);
    }

    @Override
    default void onPasteNoTag(@NotNull ServerLevel serverLevel, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull Map<Long, Long> map){};
}
