package com.verr1.controlcraft.foundation.data.control;

import com.verr1.controlcraft.foundation.api.ISerializableDynamicController;
import com.verr1.controlcraft.utils.InputChecker;
import com.verr1.controlcraft.utils.MathUtils;

public class DynamicController implements ISerializableDynamicController {
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
        integral_err = MathUtils.clamp(integral_err + curr_err * ts, MAX_INTEGRAL);
    }


    public synchronized void setTarget(double target) {
        this.target = target;
    }


    public double calculateControlValueScaleAngular() {
        return (p * MathUtils.radErrFix(curr_err) + d * MathUtils.radErrFix(curr_err - prev_err) / ts + i * integral_err);
    }

    public double calculateControlValueScaleLinear() {
        return (p * curr_err + d * (curr_err - prev_err) / ts + i * integral_err);
    }
    public double calculateControlValueScaleNonlinear() {
        double ce = MathUtils.clamp(curr_err, 2);
        double pv = Math.signum(ce) * (Math.exp(Math.abs(ce) / 0.2) - 1);
        return p * pv + d * (curr_err - prev_err) / ts + i * integral_err;
    }

    public double calculateControlValueScale(boolean angular){
        return angular ? calculateControlValueScaleAngular() : calculateControlValueScaleLinear();
    }

    public DynamicController setPID(double p, double i, double d) {
        setP(p);
        setI(i);
        setD(d);
        return this;
    }

    public DynamicController setP(double p){
        this.p = InputChecker.clampPIDInput(p);
        return this;
    }

    public DynamicController setI(double i){
        this.i = InputChecker.clampPIDInput(i);
        return this;
    }

    public DynamicController setD(double d){
        this.d = InputChecker.clampPIDInput(d);
        return this;
    }

    public DynamicController withPID(PID param){
        PID(param);
        return this;
    }

    @Override
    public void PID(PID param) {
        setPID(param.p(), param.i(), param.d());
    }

    @Override
    public PID PID() {
        return new PID(p, i, d);
    }


    public double getTarget() {
        return target;
    }

    public double getValue(){return curr;}


}
