package com.verr1.controlcraft.content.create;

import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.verr1.controlcraft.content.blocks.slider.KinematicSliderBlockEntity;
import com.verr1.controlcraft.foundation.api.IKineticPeripheral;
import com.verr1.controlcraft.foundation.executor.executables.IntervalExecutable;
import com.verr1.controlcraft.foundation.executor.Executor;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.utils.MathUtils;
import net.minecraft.nbt.CompoundTag;

public class KSliderKineticPeripheral implements IKineticPeripheral{

    private final Executor executor = new Executor();
    private final KinematicSliderBlockEntity slider;
    private float current = 0;
    private boolean enabled = true;

    public KSliderKineticPeripheral(KinematicSliderBlockEntity slider) {
        this.slider = slider;
    }

    private void updateVelocity(){
        slider.getController().setControlTarget(MathUtils.toControlCraftLinear(current));
    }

    // Create is hard to compact with in this case : (

    @Override
    public void onSpeedChanged(IKineticPeripheral.KineticContext context) {
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

                slider.getController().setControlTarget(targetDistance);
            } else if (slider.getTargetMode() == TargetMode.VELOCITY) {

                double targetDistance = MathUtils.clamp(
                        slider.getSlideDistance()
                                +
                                Math.signum(context.current())
                                        *
                                (context.context().getEffectiveValue(context.current())),
                        0, 32
                );


                double speed = Math.abs(MathUtils.toControlCraftLinear(current));

                // This Executor ticks at game thread, this task is named "p_velocity"
                // and it will run every 1 tick (20 times per second) which is exactly a control task (adjusting speed) to meet target angle
                // if a new task is added, it will cancel the previous one, because the task in a map
                // it will automatically be removed after 40 ticks (2 seconds)

                executor.execute(
                        "p_velocity",
                        new IntervalExecutable(
                                () -> {
                                    double err = (targetDistance - slider.getController().getTarget());
                                    double comp = Math.pow(10, slider.getCompliance()) ;
                                    double control = err > comp ? speed : err < -comp ? -speed : 0;
                                    slider.getController().setControlTarget(MathUtils.clamp(control, err * 20));
                                },
                                () -> slider.getController().setControlTarget(0),
                                1,
                                40
                        ));
            }
        }
        else if (slider.getTargetMode() == TargetMode.VELOCITY) {
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
