package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.verr1.vscontrolcraft.utils.VSMathUtils;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public class SpatialScheduleInfoHolder {
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

    public double getI() {
        return i;
    }

    public void setI(double i) {
        this.i = i;
    }

    public double getDp() {
        return dp;
    }

    public void setDp(double dp) {
        this.dp = dp;
    }

    public double getPp() {
        return pp;
    }

    public void setPp(double pp) {
        this.pp = pp;
    }

    protected double i = 0;

    protected double mass;
    protected double inertia;

    protected double scale;

    // assuming task run at physics thread
    protected double ts = 0.01667;

    protected double MAX_INTEGRAL = 10;

    public SpatialScheduleInfoHolder withPID(double p, double i, double d, double MAX_INTEGRAL){
        this.pp = p;
        this.i = i;
        this.dp = d;
        this.MAX_INTEGRAL = MAX_INTEGRAL;
        return this;
    }

    public Vector3dc calcControlForce(){
        Vector3dc accel_p = new Vector3d(p_err).mul(pp);
        Vector3dc accel_d = new Vector3d(p_err).sub(p_err_prev, new Vector3d()).mul(dp / ts);
        Vector3dc accel_i = new Vector3d(0, p_int.y(), 0).mul(i);
        Vector3dc force_pid = new Vector3d(accel_p).add(accel_d).add(accel_i).add(new Vector3d(0, 10, 0)).mul(mass * Math.pow(scale, 3));
        return force_pid;
    }

    public Vector3dc calcControlTorque(){
        Quaterniondc q_d = new Quaterniond(q_err).conjugate().mul(q_err_prev);
        Vector3dc accel_p = new Vector3d(q_err.x(), q_err.y(), q_err.z()).mul(pq);
        Vector3dc accel_d = new Vector3d(q_d.x(), q_d.y(), q_d.z()).mul(-2 / ts).mul(dq);
        Vector3dc torque_pd = new Vector3d(accel_p).add(accel_d).mul(inertia * Math.pow(scale, 5));
        return torque_pd;
    }

    public void overridePhysics(PhysShipImpl ship){
        mass = ship.getInertia().getShipMass();
        inertia = ship.getInertia().getMomentOfInertiaTensor().m00();

        int id = ship.getTransform().getShipToWorldScaling().minComponent();
        scale = ship.getTransform().getShipToWorldScaling().get(id);

        q_curr = ship.getTransform().getShipToWorldRotation();
        p_curr = ship.getTransform().getPositionInWorld();

        q_err_prev = new Quaterniond(q_err);
        p_err_prev = new Vector3d(p_err);

        p_err = new Vector3d(p_tar).sub(p_curr, new Vector3d());
        q_err = new Quaterniond(q_tar).mul(new Quaterniond(q_curr).conjugate());

        p_int = VSMathUtils.clamp(p_int.add(new Vector3d(p_err).mul(ts), new Vector3d()), MAX_INTEGRAL);

    }

    public void overrideTarget(Quaterniondc q_tar, Vector3dc p_tar){
        this.q_tar = q_tar;
        this.p_tar = p_tar;
        this.q_curr = q_tar;
        this.p_curr = p_tar;
    }

}
