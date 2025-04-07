package com.verr1.controlcraft.foundation.data.control;

import com.verr1.controlcraft.foundation.api.ISerializableSchedule;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import com.verr1.controlcraft.utils.InputChecker;
import com.verr1.controlcraft.utils.VSMathUtils;
import net.minecraft.nbt.CompoundTag;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;

public class SpatialSchedule implements ISerializableSchedule {
    protected Quaterniondc q_tar = new Quaterniond();
    protected Vector3dc p_tar = new Vector3d();

    protected Quaterniondc q_err_prev = new Quaterniond();
    protected Quaterniondc q_err = new Quaterniond();
    protected Quaterniondc q_curr = new Quaterniond();


    protected Vector3dc p_err_prev = new Vector3d();
    protected Vector3dc p_err = new Vector3d();
    protected Vector3dc p_int = new Vector3d();
    protected Vector3dc p_curr = new Vector3d();

    protected double pp = 18;
    protected double dp = 12;

    protected double pq = 25;
    protected double dq = 8;

    protected double ip = 0;
    protected double iq = 0;

    protected double mass;
    protected double inertia;

    protected double scale;

    // assuming task run at physics thread
    protected double ts = 0.01667;

    protected double MAX_INTEGRAL_P = 10;
    protected double MAX_INTEGRAL_Q = 10;



    public Vector3dc calcControlForce(){
        Vector3dc accel_p = new Vector3d(p_err).mul(pp);
        Vector3dc accel_d = new Vector3d(p_err).sub(p_err_prev, new Vector3d()).mul(dp / ts);
        Vector3dc accel_i = new Vector3d(0, p_int.y(), 0).mul(ip);
        Vector3dc force_pid = new Vector3d(accel_p).add(accel_d).add(accel_i).add(new Vector3d(0, 10, 0)).mul(mass * Math.pow(scale, 3));
        return force_pid;
    }

    public Vector3dc calcControlTorque(){
        Quaterniondc q_d = new Quaterniond(q_err).conjugate().mul(q_err_prev);
        double sign = q_err.w() < 0 ? -1 : 1;
        Vector3dc accel_p = new Vector3d(q_err.x(), q_err.y(), q_err.z()).mul(sign * pq);
        Vector3dc accel_d = new Vector3d(q_d.x(), q_d.y(), q_d.z()).mul(-2 / ts).mul(dq);
        Vector3dc torque_pd = new Vector3d(accel_p).add(accel_d).mul(inertia * Math.pow(scale, 5));
        return torque_pd;
    }

    public void overridePhysics(PhysShipWrapper ship){
        mass = ship.getMass();
        inertia = ship.getMomentOfInertia().m00();

        int id = ship.getTransform().getShipToWorldScaling().minComponent();
        scale = ship.getTransform().getShipToWorldScaling().get(id);

        q_curr = ship.getTransform().getShipToWorldRotation();
        p_curr = ship.getTransform().getPositionInWorld();

        q_err_prev = new Quaterniond(q_err);
        p_err_prev = new Vector3d(p_err);

        p_err = new Vector3d(p_tar).sub(p_curr, new Vector3d());
        q_err = new Quaterniond(q_tar).mul(new Quaterniond(q_curr).conjugate());

        p_int = VSMathUtils.clamp(p_int.add(new Vector3d(p_err).mul(ts), new Vector3d()), MAX_INTEGRAL_P);

    }

    public void overrideTarget(Quaterniondc q_tar, Vector3dc p_tar){
        this.q_tar = q_tar;
        this.p_tar = p_tar;
        this.q_curr = q_tar;
        this.p_curr = p_tar;
    }

    public SpatialSchedule setPq(double pq) {
        this.pq = InputChecker.clampPIDInput(pq);
        return this;
    }

    public SpatialSchedule setDq(double dq) {
        this.dq = InputChecker.clampPIDInput(dq);
        return this;
    }

    public SpatialSchedule setIq(double iq) {
        this.iq = InputChecker.clampPIDInput(iq);
        return this;
    }

    public SpatialSchedule setMAX_INTEGRAL_P(double MAX_INTEGRAL_P) {
        this.MAX_INTEGRAL_P = MAX_INTEGRAL_P;
        return this;
    }

    public SpatialSchedule setMAX_INTEGRAL_Q(double MAX_INTEGRAL_Q) {
        this.MAX_INTEGRAL_Q = MAX_INTEGRAL_Q;
        return this;
    }

    public double getIp() {
        return ip;
    }

    public SpatialSchedule setIp(double ip) {
        this.ip = InputChecker.clampPIDInput(ip);
        return this;
    }

    public double getDp() {
        return dp;
    }

    public SpatialSchedule setDp(double dp) {
        this.dp = InputChecker.clampPIDInput(dp);
        return this;
    }

    public double getPp() {
        return pp;
    }

    public SpatialSchedule setPp(double pp) {
        this.pp = InputChecker.clampPIDInput(pp);
        return this;
    }

    public SpatialSchedule withQPID(double p, double i, double d, double MAX_INTEGRAL){
        setPq(p);
        setIq(i);
        setDq(d);
        this.MAX_INTEGRAL_Q = MAX_INTEGRAL;
        return this;
    }

    public SpatialSchedule withPPID(double p, double i, double d, double MAX_INTEGRAL){
        setPp(p);
        setIp(i);
        setDp(d);
        this.MAX_INTEGRAL_P = MAX_INTEGRAL;
        return this;
    }

    @Override
    public PID QPID() {
        return new PID(pq, iq, dq);
    }

    @Override
    public PID PPID() {
        return new PID(pp, ip, dp);
    }

    @Override
    public void QPID(PID pid) {
        setPq(pid.p());
        setIq(pid.i());
        setDq(pid.d());
    }

    @Override
    public void PPID(PID pid) {
        setPp(pid.p());
        setIp(pid.i());
        setDp(pid.d());
    }


}
