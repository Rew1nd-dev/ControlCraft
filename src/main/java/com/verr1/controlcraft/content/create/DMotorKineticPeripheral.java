package com.verr1.controlcraft.content.create;

import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.verr1.controlcraft.content.blocks.motor.AbstractDynamicMotor;
import com.verr1.controlcraft.content.valkyrienskies.attachments.Observer;
import com.verr1.controlcraft.foundation.api.IKineticPeripheral;
import com.verr1.controlcraft.foundation.data.ExpirableListener;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.executor.executables.IntervalExecutable;
import com.verr1.controlcraft.foundation.executor.Executor;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.utils.MathUtils;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DMotorKineticPeripheral implements IKineticPeripheral {

    private final Executor executor = new Executor();
    private final AbstractDynamicMotor motor;
    private float current = 0;
    private boolean enabled = true;

    private int lazyTick = 0;
    private final int lazyTickRate = 20;

    public DMotorKineticPeripheral(@NotNull AbstractDynamicMotor motor) {
        this.motor = motor;
    }


    private void updateVelocity(){
        motor.setTargetAccordingly(MathUtils.toControlCraftAngular(current));
    }

    // Create is hard to compact with in this case : (

    @Override
    public void onSpeedChanged(KineticContext context) {
        if(!enabled)return;

        current = context.current();

        var ctx = context.context();
        if(ctx != null && ctx.instruction() == SequencerInstructions.TURN_ANGLE){

            if(motor.getTargetMode() == TargetMode.POSITION){

                double targetAngle = MathUtils.radianReset(
                        motor.getController().getTarget()
                                +
                                Math.signum(context.current())
                                        *
                                Math.toRadians(context.context().getEffectiveValue(context.current()))
                );

                motor.setTargetAccordingly(targetAngle);
            } else if (motor.getTargetMode() == TargetMode.VELOCITY) {

                double targetAngle = MathUtils.radianReset(
                        motor.getServoAngle()
                                +
                                Math.signum(context.current())
                                        *
                                Math.toRadians(context.context().getEffectiveValue(context.current()))
                );

                double p = 4;
                double speed = Math.abs(MathUtils.toControlCraftAngular(current));

                // This Executor ticks at physics thread, this task is named "p_velocity"
                // and it will run every 1 tick (60 times per second) which is exactly a control task (adjusting speed) to meet target angle
                // if a new task is added, it will cancel the previous one, because the task in a map
                // it will automatically be removed after 120 ticks (2 seconds)

                executor.execute(
                        "p_velocity",
                        new IntervalExecutable(
                                () -> motor.setTargetAccordingly(
                                    MathUtils.clamp(
                                        p * MathUtils.radErrFix(targetAngle - motor.getServoAngle()), // Control Formula MathUtils.radErrFix
                                        speed
                                    )
                                ),
                                () -> motor.setTargetAccordingly(0),
                                1,
                                120
                        ));
            }
        }
        else if (motor.getTargetMode() == TargetMode.VELOCITY) {
            updateVelocity();
        }
    }


    // This is basically asking Observer (Run at physics thread) to tick executor every physics tick.
    // this listener expires after 80 ticks (roughly 1.67 seconds)
    // and this class refresh (recreate) a new one every 1 second
    // A blockPos can only have one listener, since the key is WorldBlockPos


    public void syncInducer(){
        Optional
            .ofNullable(motor.getCompanionServerShip())
            .map(Observer::getOrCreate)
            .ifPresent(inducer -> inducer.replace(
                    WorldBlockPos.of(motor.getLevel(), motor.getBlockPos()),
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
