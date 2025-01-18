package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.verr1.vscontrolcraft.base.IntervalExecutor.IntervalRunnable;
import com.verr1.vscontrolcraft.compat.valkyrienskies.generic.QueueForceInducer;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.impl.shadow.B;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.lang.Math;
import java.util.Queue;

public class ServoConstrainAssembleSchedule implements IntervalRunnable {
    private int cyclesRemained = 0;


    private BlockPos servoPos;
    private BlockPos assemPos;
    private ServerLevel level;
    private Direction servoDirection;
    private Direction assemDirection;

    // running data
    private Quaterniondc q_tar = new Quaterniond();
    private Vector3dc p_tar = new Vector3d();

    private Quaterniondc q_err_prev = new Quaterniond();
    private Quaterniondc q_err = new Quaterniond();
    private Quaterniondc q_curr = new Quaterniond();


    private Vector3dc p_err_prev = new Vector3d();
    private Vector3dc p_err = new Vector3d();
    private Vector3dc p_int = new Vector3d();
    private Vector3dc p_curr = new Vector3d();

    double p = 18;
    double d = 12;
    double i = 3;

    double mass;
    double inertia;

    // assuming task run at game thread
    double ts = 0.05;



    @Override
    public int getCyclesRemained() {
        return cyclesRemained;
    }

    @Override
    public int getIntervalTicks() {
        // always run per tick
        return -1;
    }

    @Override
    public void reset() {

    }

    @Override
    public void tickDown() {

    }

    @Override
    public void cycleDown() {
        cyclesRemained--;
    }

    @Override
    public void onExpire() {
        if(!shouldApplyConstrain())return;
        BlockEntity be = level.getExistingBlockEntity(servoPos);
        if(be instanceof ServoMotorBlockEntity servo){
            servo.assemble(assemPos, assemDirection);
        }
    }

    public boolean shouldApplyConstrain(){
        return p_err.lengthSquared() < 1e-2 && q_err.angle() < 1e-2;
    }

    public Vector3dc getServoFacePos(){
        ServerShip servoShip = VSGameUtilsKt.getShipObjectManagingPos(level, servoPos);
        if(servoShip == null)return Util.Vec3toVector3d(servoPos.relative(servoDirection).getCenter());
        Vector3dc servoFace_sc = Util.Vec3toVector3d(servoPos.relative(servoDirection).getCenter());
        Vector3dc servoFace_wc = servoShip.getTransform().getShipToWorld().transformPosition(servoFace_sc, new Vector3d());
        return servoFace_wc;
    }

    public Quaterniondc getServoBaseQuaternion(){
        ServerShip servoShip = VSGameUtilsKt.getShipObjectManagingPos(level, servoPos);
        if(servoShip == null)return new Quaterniond();
        Quaterniondc servoBaseQuaternion = servoShip.getTransform().getShipToWorldRotation();
        return servoBaseQuaternion;
    }

    public Quaterniondc getAssemTargetQuaternion(){
        Quaterniondc servoBase = getServoBaseQuaternion();
        Quaterniondc alignExtra = VSMathUtils.rotationToAlign(servoDirection, assemDirection);
        return servoBase.mul(alignExtra, new Quaterniond());
    }

    public Vector3dc getAssemTargetPosition(){
        ServerShip assemShip = VSGameUtilsKt.getShipObjectManagingPos(level, assemPos);
        if(assemShip == null)return new Vector3d(0, 0, 0);
        Vector3dc dir = Util.Vec3itoVector3d(assemDirection.getNormal()).mul(0.2);
        Vector3dc assemFace_sc = Util.Vec3itoVector3d(assemPos).add(dir);
        Vector3dc assemCenter_sc = assemShip.getInertiaData().getCenterOfMassInShip();
        Vector3dc relative_r_sc = new Vector3d(assemFace_sc).sub(assemCenter_sc, new Vector3d());

        Quaterniondc targetQuaternion = getAssemTargetQuaternion();
        Vector3dc relative_r_wc = targetQuaternion.transform(relative_r_sc, new Vector3d());
        Vector3dc servoFace_wc = getServoFacePos();
        Vector3dc assemCenter_target = new Vector3d(servoFace_wc).sub(relative_r_wc, new Vector3d());
        return assemCenter_target;
    }

    public Vector3dc calcControlForce(){
        Vector3dc accel_p = new Vector3d(p_err).mul(p);
        Vector3dc accel_d = new Vector3d(p_err).sub(p_err_prev, new Vector3d()).mul(d / ts);
        Vector3dc accel_i = new Vector3d(0, p_int.y(), 0).mul(i);
        Vector3dc force_pid = new Vector3d(accel_p).add(accel_d).add(accel_i).add(new Vector3d(0, 10, 0)).mul(mass);
        return force_pid;
    }

    public Vector3dc calcControlTorque(){

        Quaterniondc q_d = new Quaterniond(q_err).conjugate().mul(q_err_prev);

        Vector3dc accel_p = new Vector3d(q_err.x(), q_err.y(), q_err.z()).mul(p);
        Vector3dc accel_d = new Vector3d(q_d.x(), q_d.y(), q_d.z()).mul(-2 / ts).mul(d);

        Vector3dc torque_pd = new Vector3d(accel_p).add(accel_d).mul(inertia);
        return torque_pd;
    }

    public ServoConstrainAssembleSchedule(
            BlockPos servoPos,
            Direction servoDirection,
            BlockPos assemPos,
            Direction assemDirection,
            ServerLevel level,
            int timeBeforeExpired
    ){
        this.servoPos = servoPos;
        this.servoDirection = servoDirection;
        this.assemPos = assemPos;
        this.assemDirection = assemDirection;
        this.level = level;
        this.cyclesRemained = (int)(timeBeforeExpired / ts);

        q_tar = getAssemTargetQuaternion();
        p_tar = getAssemTargetPosition();
    }

    @Override
    public void run() {

        ServerShip assemShip = VSGameUtilsKt.getShipObjectManagingPos(level, assemPos);
        if(assemShip == null)return;
        QueueForceInducer qfi = QueueForceInducer.getOrCreate(assemShip);
        mass = assemShip.getInertiaData().getMass();
        inertia = assemShip.getInertiaData().getMomentOfInertiaTensor().m00();



        q_curr = assemShip.getTransform().getShipToWorldRotation();
        p_curr = assemShip.getTransform().getPositionInWorld();

        q_err_prev = new Quaterniond(q_err);
        p_err_prev = new Vector3d(p_err);

        p_err = new Vector3d(p_tar).sub(p_curr, new Vector3d());
        q_err = new Quaterniond(q_tar).mul(new Quaterniond(q_curr).conjugate());

        p_int = VSMathUtils.clamp(p_int.add(new Vector3d(p_err).mul(ts), new Vector3d()), 10);


        Vector3dc force = calcControlForce();
        Vector3dc torque = calcControlTorque();

        qfi.applyInvariantTorque(torque);
        qfi.applyInvariantForce(force);
    }
}
