package com.verr1.vscontrolcraft.base.Servo;

import com.verr1.vscontrolcraft.utils.VSMathUtils;

public class ControllerInfoHolder {
    private double curr_err = 0;
    private double prev_err = 0;

    private double integral_err = 0;
    private double MAX_INTEGRAL = 10;

    private double p = 24;
    private double d = 4;
    private double i = 0;
    private double ts = 0.01667; // assuming servo controlled by physics thread

    private double targetAngle = 1.57;

    public synchronized ControllerInfoHolder overrideError(double angle) {
        prev_err = curr_err;
        curr_err = targetAngle - angle;
        integral_err = VSMathUtils.clamp(integral_err + curr_err * ts, MAX_INTEGRAL);
        return this;
    }

    public synchronized ControllerInfoHolder setTargetAngle(double angle) {
        targetAngle = angle;
        return this;
    }


    public double calculateControlTorqueScale() {
        double scale = p * curr_err + d * VSMathUtils.radErrFix(curr_err - prev_err) / ts + i * integral_err;
        return scale;
    }

    public ControllerInfoHolder setParameter(double p, double d, double i) {
        this.p = p;
        this.d = d;
        this.i = i;
        return this;
    }

    public ControllerInfoHolder setParameter(PID param) {
        p = param.p();
        i = param.i();
        d = param.d();
        return this;
    }

    public PID getPIDParams() {
        return new PID(p, i, d);
    }


    public double getTargetAngle() {
        return targetAngle;
    }
}
