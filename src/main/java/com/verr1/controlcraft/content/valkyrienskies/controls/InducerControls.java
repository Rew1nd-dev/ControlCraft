package com.verr1.controlcraft.content.valkyrienskies.controls;

import com.verr1.controlcraft.foundation.data.logical.*;
import com.verr1.controlcraft.utils.VSMathUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.mod.api.ValkyrienSkies;

/*
*   This is what makes Control Craft to be Control Craft :)
*
* */

public class InducerControls {

    public static void anchorTickControls(LogicalAnchor anchor, PhysShip physShip) {
        Vector3dc velocity = physShip.getVelocity();
        Vector3dc fAirResistance = velocity.mul(-anchor.airResist() * physShip.getMass(), new Vector3d());
        Vector3dc fExtraGravity = new Vector3d(0, -physShip.getMass(), 0).mul(anchor.extraGravity());


        double ts = 0.01667;
        int id = physShip.getTransform().getShipToWorldScaling().minComponent();
        // double scale = physShip.getTransform().getShipToWorldScaling().get(id);
        double inertia = physShip.getMomentOfInertia().m00();

        Vector3dc q_d = physShip.getAngularVelocity();
        Vector3dc accel_d = new Vector3d(q_d.x(), q_d.y(), q_d.z()).mul(-2 / ts).mul(anchor.rotDamp());
        Vector3dc tRotationalResistance = new Vector3d(accel_d).mul(inertia);

        physShip.applyInvariantForce(fExtraGravity);
        physShip.applyInvariantForce(fAirResistance);
        physShip.applyInvariantTorque(tRotationalResistance);
    }


    public static void motorTickControls(LogicalMotor motor, PhysShip motorShip, PhysShip compShip) {
        double metric = motor.angleOrSpeed() ?
                VSMathUtils.get_yc2xc(motorShip, compShip, motor.motorDir(), motor.compDir()) :
                VSMathUtils.get_dyc2xc(motorShip, compShip, motorShip.getOmega(), compShip.getOmega(), motor.motorDir(), motor.compDir());


        double accel_scale = VSMathUtils.clamp(motor.controller().calculateControlValueScale(motor.angleOrSpeed()), 1000);
        double control_torque = motor.torque();
        double internal_torque = compShip.getMomentOfInertia().m00() * accel_scale;
        Vector3dc direction = ValkyrienSkies.set(new Vector3d(), motor.motorDir().getNormal());

        Vector3dc controlTorque_sc = direction.mul((-control_torque + internal_torque) * -1  , new Vector3d()); //
        Vector3dc controlTorque_wc = VSMathUtils.get_sc2wc(motorShip).transform(controlTorque_sc, new Vector3d());

        motor.controller().overrideError(metric);

        //
        compShip.applyInvariantTorque(controlTorque_wc);
        if(!motor.shouldCounter()) return;
        motorShip.applyInvariantTorque(controlTorque_wc.mul(-1, new Vector3d()));
    }


    public static void sliderTickControls(LogicalSlider slider, PhysShip selfShip, PhysShip compShip){
        Vector3dc own_local_pos = slider.selfContact();
        Vector3dc cmp_local_pos = slider.compContact();

        Matrix4dc own_s2w = selfShip.getShipToWorld();
        Matrix4dc own_w2s = selfShip.getWorldToShip();
        Matrix4dc cmp_s2w = compShip.getShipToWorld();

        Vector3dc own_wc = own_s2w.transformPosition(own_local_pos, new Vector3d());
        Vector3dc cmp_wc = cmp_s2w.transformPosition(cmp_local_pos, new Vector3d());
        Vector3dc sub_sc = own_w2s
                .transformDirection(
                        cmp_wc.sub(own_wc, new Vector3d()), new Vector3d()
                );

        Direction dir = slider.slideDir();
        double sign = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
        double distance = switch (dir.getAxis()){
            case X -> sign * sub_sc.x();
            case Y -> sign * sub_sc.y();
            case Z -> sign * sub_sc.z();
        };


        double metric = slider.positionOrSpeed() ?
                distance :
                VSMathUtils.get_dy2x_sc(selfShip, compShip, slider.slideDir());

        double extra = slider.force();
        double mass = compShip.getMass();
        double scale = slider.controller().calculateControlValueScaleLinear();
        double clampedScale = VSMathUtils.clamp(scale, 1000);


        slider.controller().overrideError(metric);
        Vector3dc dirJOML = ValkyrienSkies.set(new Vector3d(), dir.getNormal());
        Vector3dc dirJOML_wc = own_s2w.transformDirection(new Vector3d(dirJOML));
        double cos = dirJOML_wc.angleCos(new Vector3d(0, 1, 0));
        Vector3dc controlForce_sc = dirJOML.mul((clampedScale + cos * 10) * mass + extra, new Vector3d());
        Vector3dc controlForce_wc = own_s2w.transformDirection(controlForce_sc, new Vector3d());

        Vector3dc own_r = new Vector3d(own_local_pos).sub(selfShip.getTransform().getPositionInShip());
        Vector3dc cmp_r = new Vector3d(cmp_local_pos).sub(compShip.getTransform().getPositionInShip());

        compShip.applyInvariantForceToPos(controlForce_wc.mul( 1, new Vector3d()), cmp_r);
        if(!slider.shouldCounter())return;
        selfShip.applyInvariantForceToPos(controlForce_wc.mul(-1, new Vector3d()), own_r);

    }

    public static void spatialTickControls(LogicalSpatial spatial, PhysShip physShip){
        spatial.schedule().overridePhysics(physShip);
        Vector3dc controlTorque = spatial.schedule().calcControlTorque();
        Vector3dc controlForce  = spatial.schedule().calcControlForce();

        physShip.applyInvariantForce(controlForce);
        physShip.applyInvariantTorque(controlTorque);
    }

    public static void jetTickControls(LogicalJet jet, PhysShip physShip) {
        Vector3dc dir = jet.direction();
        double thrust = jet.thrust();

        Vector3dc force_sc = dir.mul(thrust, new Vector3d());
        Vector3dc force_wc = physShip.getTransform().getShipToWorld().transformDirection(force_sc, new Vector3d());

        Vector3dc ship_sc = physShip.getTransform().getPositionInShip();
        Vector3dc jet_sc = ValkyrienSkies.set(new Vector3d(), jet.pos().pos().getCenter());
        Vector3dc relativeRadius_sc = jet_sc.sub(ship_sc, new Vector3d());

        physShip.applyInvariantForceToPos(force_wc, relativeRadius_sc);
    }

    public static void propellerTickControls(LogicalPropeller propeller, PhysShip physShip) {
        Vector3d p_sc = ValkyrienSkies.set(new Vector3d(), propeller.pos().pos().getCenter());
        Vector3d s_sc = (Vector3d) physShip.getTransform().getPositionInShip();
        Vector3d r_sc = p_sc.sub(s_sc);

        Vector3d torque = new Vector3d(propeller.direction()).mul(propeller.speed() * propeller.THRUST_RATIO());
        Vector3d thrust = new Vector3d(propeller.direction()).mul(propeller.speed() * propeller.TORQUE_RATIO());

        Vector3d torque_wc = physShip.getTransform().getShipToWorld().transformDirection(torque);
        Vector3d thrust_wc = physShip.getTransform().getShipToWorld().transformDirection(thrust);

        physShip.applyInvariantForceToPos(thrust_wc, r_sc);
        physShip.applyInvariantTorque(torque_wc);
    }
}
