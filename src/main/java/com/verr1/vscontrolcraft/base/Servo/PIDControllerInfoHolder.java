package com.verr1.vscontrolcraft.base.Servo;

import com.verr1.vscontrolcraft.utils.InputChecker;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.nbt.CompoundTag;

public class PIDControllerInfoHolder {
    private double curr_err = 0;
    private double prev_err = 0;

    private double curr = 0;

    private double integral_err = 0;
    private final double MAX_INTEGRAL = 100;

    private double p = 0;
    private double d = 0;
    private double i = 0;
    private final double ts = 0.01667; // assuming servo controlled by physics thread

    private double target = 0;

    public synchronized void overrideError(double value) {
        curr = value;
        prev_err = curr_err;
        curr_err = target - value;
        integral_err = VSMathUtils.clamp(integral_err + curr_err * ts, MAX_INTEGRAL);
    }


    public synchronized void setTarget(double target) {
        this.target = target;
    }


    public double calculateControlValueScaleAngular() {
        return (p * VSMathUtils.radErrFix(curr_err) + d * VSMathUtils.radErrFix(curr_err - prev_err) / ts + i * integral_err);
    }

    public double calculateControlValueScaleLinear() {
        return (p * curr_err + d * (curr_err - prev_err) / ts + i * integral_err);
    }
    public double calculateControlValueScaleNonlinear() {
        double ce = VSMathUtils.clamp(curr_err, 2);
        double pv = Math.signum(ce) * (Math.exp(Math.abs(ce) / 0.2) - 1);
        return p * pv + d * (curr_err - prev_err) / ts + i * integral_err;
    }

    public double calculateControlValueScale(boolean angular){
        return angular ? calculateControlValueScaleAngular() : calculateControlValueScaleLinear();
    }

    public PIDControllerInfoHolder setParameter(double p, double i, double d) {
        setP(p);
        setI(i);
        setD(d);
        return this;
    }

    public PIDControllerInfoHolder setP(double p){
        this.p = InputChecker.clampPIDInput(p);
        return this;
    }

    public PIDControllerInfoHolder setI(double i){
        this.i = InputChecker.clampPIDInput(i);
        return this;
    }

    public PIDControllerInfoHolder setD(double d){
        this.d = InputChecker.clampPIDInput(d);
        return this;
    }

    public PIDControllerInfoHolder setParameter(PID param) {
        setParameter(param.p(), param.i(), param.d());
        return this;
    }

    public PID getPIDParams() {
        return new PID(p, i, d);
    }


    public double getTarget() {
        return target;
    }

    public double getValue(){return curr;}


    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putDouble("p", p);
        tag.putDouble("i", i);
        tag.putDouble("d", d);
        tag.putDouble("target", target);
        return tag;
    }

    public void deserialize(CompoundTag tag){
        p = tag.getDouble("p");
        i = tag.getDouble("i");
        d = tag.getDouble("d");
        target = tag.getDouble("target");
    }

}
