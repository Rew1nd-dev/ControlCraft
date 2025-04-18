package com.verr1.controlcraft.content.create;

import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.attachments.Observer;
import com.verr1.controlcraft.foundation.api.IKineticPeripheral;
import com.verr1.controlcraft.foundation.data.ExpirableListener;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.executor.IntervalExecutable;
import com.verr1.controlcraft.foundation.executor.Executor;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.utils.MathUtils;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.verr1.controlcraft.utils.MathUtils.toControlCraftLinear;

public class DSliderKineticPeripheral implements IKineticPeripheral {

    private final Executor executor = new Executor();
    private final DynamicSliderBlockEntity slider;
    private float current = 0;
    private boolean enabled = true;

    private int lazyTick = 0;
    private final int lazyTickRate = 20;

    public DSliderKineticPeripheral(@NotNull DynamicSliderBlockEntity motor) {
        this.slider = motor;
    }


    private void updateVelocity(){
        slider.setTarget(toControlCraftLinear(current));
    }

    // Create is hard to compact with in this case : (

    @Override
    public void onSpeedChanged(KineticContext context) {
        if(!enabled)return;

        current = context.current();

        var ctx = context.context();
        if(ctx != null && ctx.instruction() == SequencerInstructions.TURN_DISTANCE){

            if(slider.getTargetMode() == TargetMode.POSITION){

                double targetDistance = MathUtils.clamp(
                        slider.getController().getTarget()
                                +
                                Math.signum(context.current())
                                        *
                                (context.context().getEffectiveValue(context.current())),
                        0, 32
                );

                slider.setTarget(targetDistance);
            } else if (slider.getTargetMode() == TargetMode.VELOCITY) {

                double targetDistance = MathUtils.clamp(
                        slider.getSlideDistance()
                                +
                                Math.signum(context.current())
                                        *
                                context.context().getEffectiveValue(context.current()),
                        0, 32
                );

                double p = 4;
                double speed = toControlCraftLinear(context.current()); // Create Speed is in m/t

                // This Executor ticks at physics thread, this task is named "p_velocity"
                // and it will run every 1 tick (60 times per second) which is exactly a control task (adjusting speed) to meet target angle
                // if a new task is added, it will cancel the previous one, because the task in a map
                // it will automatically be removed after 120 ticks (2 seconds)

                executor.execute(
                        "p_velocity",
                        new IntervalExecutable(
                                () -> slider.setTarget(
                                        MathUtils.clamp(
                                                p * (targetDistance - slider.getSlideDistance()), // Control Formula MathUtils.radErrFix
                                                speed
                                        )
                                ),
                                () -> slider.setTarget(0),
                                1,
                                120
                        ));
            }
        }
        else if (slider.getTargetMode() == TargetMode.VELOCITY) {
            updateVelocity();
        }
    }


    // This is basically asking Observer (Run at physics thread) to tick executor every physics tick.
    // this listener expires after 80 ticks (roughly 1.67 seconds)
    // and this class refresh (recreate) a new one every 1 second
    // A blockPos can only have one listener, since the key is WorldBlockPos


    public void syncInducer(){
        Optional
                .ofNullable(slider.getCompanionServerShip())
                .map(Observer::getOrCreate)
                .ifPresent(inducer -> inducer.replace(
                        WorldBlockPos.of(slider.getLevel(), slider.getBlockPos()),
                        new ExpirableListener<>($ -> executor.tick(), 80) // don't need ship physics input
                ));
    }


    public void lazyTick(){
        if(--lazyTick > 0)return;
        lazyTick = lazyTickRate;

        syncInducer();
    }

    public void tick(){
        if(!enabled)return;

        lazyTick();

        // executor.tick();
    }

    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putFloat("speed", current);
        tag.putBoolean("enabled", enabled);
        return tag;
    }

    public void deserialize(CompoundTag tag){
        current = tag.getFloat("speed");
        enabled = tag.getBoolean("enabled");
    }


}
