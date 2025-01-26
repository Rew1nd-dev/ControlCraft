package com.verr1.vscontrolcraft.base.Servo;

import com.verr1.vscontrolcraft.utils.VSMathUtils;

public class PIDControllerInfoHolder {
    private double curr_err = 0;
    private double prev_err = 0;

    private double integral_err = 0;
    private double MAX_INTEGRAL = 10;

    private double p = 24;
    private double d = 14;
    private double i = 0;
    private double ts = 0.01667; // assuming servo controlled by physics thread

    private double target = 1.57;

    public synchronized void overrideError(double value) {
        prev_err = curr_err;
        curr_err = target - value;
        integral_err = VSMathUtils.clamp(integral_err + curr_err * ts, MAX_INTEGRAL);
    }

    public synchronized void setTarget(double target) {
        this.target = target;
    }


    public double calculateControlValueScaleAngular() {
        return p * curr_err + d * VSMathUtils.radErrFix(curr_err - prev_err) / ts + i * integral_err;
    }

    public double calculateControlValueScaleLinear() {
        return p * curr_err + d * (curr_err - prev_err) / ts + i * integral_err;
    }
    public double calculateControlValueScaleNonlinear() {
        double ce = VSMathUtils.clamp(curr_err, 2);
        double pv = Math.signum(ce) * (Math.exp(Math.abs(ce) / 0.2) - 1);
        return p * pv + d * (curr_err - prev_err) / ts + i * integral_err;
    }

    public PIDControllerInfoHolder setParameter(double p, double d, double i) {
        this.p = p;
        this.d = d;
        this.i = i;
        return this;
    }

    public PIDControllerInfoHolder setParameter(PID param) {
        p = param.p();
        i = param.i();
        d = param.d();
        return this;
    }

    public PID getPIDParams() {
        return new PID(p, i, d);
    }


    public double getTarget() {
        return target;
    }
}
