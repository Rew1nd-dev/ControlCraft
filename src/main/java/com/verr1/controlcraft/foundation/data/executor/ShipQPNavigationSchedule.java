package com.verr1.controlcraft.foundation.data.executor;

import com.verr1.controlcraft.content.valkyrienskies.attachments.QueueForceInducer;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.utils.MathUtils;
import com.verr1.controlcraft.utils.VSGetterUtils;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

public class ShipQPNavigationSchedule extends IntervalExecutable {

    protected WorldBlockPos shipPos;

    // running data
    protected Quaterniondc q_tar = new Quaterniond();
    protected Vector3dc p_tar = new Vector3d();

    protected Quaterniondc q_err_prev = new Quaterniond();
    protected Quaterniondc q_err = new Quaterniond();
    protected Quaterniondc q_curr = new Quaterniond();


    protected Vector3dc p_err_prev = new Vector3d();
    protected Vector3dc p_err = new Vector3d();
    protected Vector3dc p_int = new Vector3d();
    protected Vector3dc p_curr = new Vector3d();

    protected double p = 12;
    protected double d = 12;
    protected double i = 1;

    protected double mass;
    protected double inertia;

    protected double scale;

    // assuming task run at game thread
    protected double ts = 0.05;

    protected double MAX_INTEGRAL = 10;

    public ShipQPNavigationSchedule(WorldBlockPos levelPos, Quaterniond quaterniond, Vector3d vector3d, int timeBeforeExpired) {
        super(() -> {}, 1, timeBeforeExpired * 20);
        this.shipPos = levelPos;
        this.q_tar = quaterniond;
        this.p_tar = vector3d;
    }


    public ShipQPNavigationSchedule setPID(double p, double i, double d, double MAX_INTEGRAL){
        this.p = p;
        this.i = i;
        this.d = d;
        this.MAX_INTEGRAL = MAX_INTEGRAL;
        return this;
    }

    public Vector3dc calcControlForce(){
        Vector3dc accel_p = new Vector3d(p_err).mul(p);
        Vector3dc accel_d = new Vector3d(p_err).sub(p_err_prev, new Vector3d()).mul(d / ts);
        Vector3dc accel_i = new Vector3d(0, p_int.y(), 0).mul(i);
        return new Vector3d(accel_p).add(accel_d).add(accel_i).add(new Vector3d(0, 10, 0)).mul(mass * Math.pow(scale, 3));
    }

    public Vector3dc calcControlTorque(){

        Quaterniondc q_d = new Quaterniond(q_err).conjugate().mul(q_err_prev);
        double sign = q_err.w() > 0 ? 1 : -1;
        Vector3dc accel_p = new Vector3d(q_err.x(), q_err.y(), q_err.z()).mul(sign * p);
        Vector3dc accel_d = new Vector3d(q_d.x(), q_d.y(), q_d.z()).mul(-2 / ts).mul(d);

        return new Vector3d(accel_p).add(accel_d).mul(inertia * Math.pow(scale, 5));
    }

    @Override
    public void onRemove() {
        onExpired();
    }

    public void onExpired(){

    }

    @Override
    public void run() {
        LoadedServerShip ship = VSGetterUtils.getLoadedServerShip(shipPos).orElse(null);
        if(ship == null)return;
        QueueForceInducer qfi = QueueForceInducer.getOrCreate(ship);
        mass = ship.getInertiaData().getMass();
        inertia = ship.getInertiaData().getMomentOfInertiaTensor().m00();

        int id = ship.getTransform().getShipToWorldScaling().minComponent();
        scale = ship.getTransform().getShipToWorldScaling().get(id);

        q_curr = ship.getTransform().getShipToWorldRotation();
        p_curr = ship.getTransform().getPositionInWorld();

        q_err_prev = new Quaterniond(q_err);
        p_err_prev = new Vector3d(p_err);

        p_err = new Vector3d(p_tar).sub(p_curr, new Vector3d());
        q_err = new Quaterniond(q_tar).mul(new Quaterniond(q_curr).conjugate());

        p_int = MathUtils.clamp(p_int.add(new Vector3d(p_err).mul(ts), new Vector3d()), MAX_INTEGRAL);


        Vector3dc force = calcControlForce();
        Vector3dc torque = calcControlTorque();

        qfi.applyInvariantTorque(torque);
        qfi.applyInvariantForce(force);
    }
}
