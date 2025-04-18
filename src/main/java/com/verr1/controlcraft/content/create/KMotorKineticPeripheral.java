package com.verr1.controlcraft.content.create;

import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.verr1.controlcraft.content.blocks.motor.AbstractDynamicMotor;
import com.verr1.controlcraft.content.blocks.motor.AbstractKinematicMotor;
import com.verr1.controlcraft.content.valkyrienskies.attachments.Observer;
import com.verr1.controlcraft.foundation.api.IKineticPeripheral;
import com.verr1.controlcraft.foundation.data.ExpirableListener;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.executor.IntervalExecutable;
import com.verr1.controlcraft.foundation.executor.Executor;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.utils.MathUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class KMotorKineticPeripheral implements IKineticPeripheral {

    private final Executor executor = new Executor();
    private final AbstractKinematicMotor motor;
    private float current = 0;
    private boolean enabled = true;

    public KMotorKineticPeripheral(AbstractKinematicMotor motor) {
        this.motor = motor;
    }

    private void updateVelocity(){
        motor.getController().setControlTarget(MathUtils.toControlCraftAngular(current));
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

                motor.getController().setControlTarget(targetAngle);
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

                // This Executor ticks at game thread, this task is named "p_velocity"
                // and it will run every 1 tick (20 times per second) which is exactly a control task (adjusting speed) to meet target angle
                // if a new task is added, it will cancel the previous one, because the task in a map
                // it will automatically be removed after 40 ticks (2 seconds)

                executor.execute(
                        "p_velocity",
                        new IntervalExecutable(
                                () -> {
                                    double err = MathUtils.radianReset(targetAngle - motor.getController().getTarget());
                                    double comp = Math.pow(10, motor.getCompliance()) ;
                                    double control = err > comp ? speed : err < -comp ? -speed : 0;
                                    motor.getController().setControlTarget(MathUtils.clamp(control, err * 20));
                                },
                                () -> motor.getController().setControlTarget(0),
                                1,
                                40
                        ));
            }
        }
        else if (motor.getTargetMode() == TargetMode.VELOCITY) {
            updateVelocity();
        }
    }


    public void tick(){
        if(!enabled)return;
        executor.tick();
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
