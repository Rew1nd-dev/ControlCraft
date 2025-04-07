package com.verr1.controlcraft.foundation.data.control;

import com.verr1.controlcraft.utils.MathUtils;
import net.minecraft.nbt.CompoundTag;

public class KinematicController {
    private double target = 0;
    private double controlTarget = 0;


    public double getControlTarget() {
        return controlTarget;
    }

    public void setControlTarget(double controlTarget) {
        this.controlTarget = controlTarget;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public void updateTargetAngular(double ts){
        target = MathUtils.radianReset(target + controlTarget * ts);
    }

    public void updateTargetLinear(double ts, double min, double max){
        target = MathUtils.clamp(target + controlTarget * ts, min, max);
    }

    public void updateForcedTarget(){
        target = controlTarget;
    }

    public void updateLerpedLinearTarget(double speed, double ts){
        double sign = Math.signum(controlTarget - target);
        double step = sign * Math.abs(speed) * ts;

        target = isApproaching(step) ? target + step : controlTarget;

    }

    private boolean isApproaching(double delta){
        return Math.signum(controlTarget - target) == Math.signum(controlTarget - target - delta);
    }



    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        // tag.putDouble("target", target);
        tag.putDouble("control_target", controlTarget);
        return tag;
    }

    public void deserialize(CompoundTag tag){
        // target = tag.getDouble("target");
        controlTarget = tag.getDouble("control_target");
    }




}
