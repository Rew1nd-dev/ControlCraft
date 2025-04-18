package com.verr1.controlcraft.foundation.api;

import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface IKineticPeripheral {

    void onSpeedChanged(
            KineticContext context
    );


    record KineticContext(float current, float prev, @Nullable SequencedGearshiftBlockEntity.SequenceContext context){}

}
