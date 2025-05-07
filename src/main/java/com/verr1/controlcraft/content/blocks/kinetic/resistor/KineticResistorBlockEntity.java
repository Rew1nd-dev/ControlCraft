package com.verr1.controlcraft.content.blocks.kinetic.resistor;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.content.blocks.NetworkBlockEntity;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class KineticResistorBlockEntity extends SplitShaftBlockEntity implements
        ITerminalDevice
{

    private double ratio = 1.0;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    this::ratio,
                    this::setRatio,
                    "ratio",
                    ExposedFieldType.RATIO
            )
                    .withSuggestedRange(0.0, 1.0),
            new ExposedFieldWrapper(
                    this::ratio,
                    this::setRatio,
                    "ratio",
                    ExposedFieldType.RATIO$1
            )
                    .withSuggestedRange(0.0, 1.0)
    );


    public KineticResistorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);



    }

    public double ratio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        return (float) ratio;
    }


    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "resistor";
    }
}
