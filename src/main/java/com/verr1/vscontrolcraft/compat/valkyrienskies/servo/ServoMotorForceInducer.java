package com.verr1.vscontrolcraft.compat.valkyrienskies.servo;

import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.compat.valkyrienskies.base.AbstractExpirableForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.generic.PhysShipWrapper;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import kotlin.jvm.functions.Function1;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public class ServoMotorForceInducer extends AbstractExpirableForceInducer {

    public static ServoMotorForceInducer getOrCreate(@NotNull ServerShip ship){
        ServoMotorForceInducer obj = ship.getAttachment(ServoMotorForceInducer.class);
        if(obj == null){
            obj = new ServoMotorForceInducer();
            ship.saveAttachment(ServoMotorForceInducer.class, obj);
        }
        return obj;
    }

    public void servoControl(BlockPos servoPos, PhysShipWrapper servShip, PhysShipWrapper compShip, LogicalServoMotor property){
        BlockEntity be = property.level().getExistingBlockEntity(servoPos);
        if(!(be instanceof AbstractServoMotor servo))return;


        double metric = property.angleOrSpeed() ?
                VSMathUtils.get_yc2xc(servShip, compShip, property.servDir(), property.compDir()) :
                VSMathUtils.get_dyc2xc(servShip, compShip, servShip.getOmega(), compShip.getOmega(), property.servDir(), property.compDir());


        // servo.debug_angle_accessor = angle;

        int id = compShip.getImpl().getTransform().getShipToWorldScaling().minComponent();
        double scale_i = compShip.getImpl().getTransform().getShipToWorldScaling().get(id);
        double inertia_scale_ratio = Math.pow(scale_i, 5);

        double accel_scale = servo.getControllerInfoHolder().calculateControlValueScale(property.angleOrSpeed());
        accel_scale = VSMathUtils.clamp(accel_scale, 1000);
        double control_torque = property.torque();
        double internal_torque = compShip.getImpl().getInertia().getMomentOfInertiaTensor().m00() * inertia_scale_ratio * accel_scale;
        Vector3dc direction = Util.Vec3itoVector3d(property.servDir().getNormal());

        Vector3dc controlTorque_sc = direction.mul((-control_torque + internal_torque) * -1  , new Vector3d()); //
        Vector3dc controlTorque_wc = VSMathUtils.get_sc2wc(servShip).transform(controlTorque_sc, new Vector3d());

        servo.getControllerInfoHolder().overrideError(metric);

        //
        compShip.getImpl().applyInvariantTorque(controlTorque_wc);
        if(property.shouldCounter())
            servShip.getImpl().applyInvariantTorque(controlTorque_wc.mul(-1, new Vector3d()));
    }

    public void writePhysics(BlockPos servoPos, PhysShipWrapper servoShip, PhysShipWrapper assemShip, LogicalServoMotor property){
        BlockEntity be = property.level().getExistingBlockEntity(servoPos);
        if(!(be instanceof AbstractServoMotor servo))return;
        servo.ownPhysics.write(VSMathUtils.getShipPhysics(servoShip.getImpl()));
        servo.asmPhysics.write(VSMathUtils.getShipPhysics(assemShip.getImpl()));
    }



    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        super.applyForces(physShip);

    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        PhysShipWrapper assemShip = new PhysShipWrapper((PhysShipImpl) physShip);
        getLives().forEach(
            (levelPos, integer) -> {
                if(!(VSMathUtils.getExisting(levelPos) instanceof AbstractServoMotor servo))return;
                LogicalServoMotor logicalServoMotor = servo.getLogicalServoMotor();
                if(logicalServoMotor == null)return;
                PhysShipWrapper servoShip = new PhysShipWrapper((PhysShipImpl) lookupPhysShip.invoke(logicalServoMotor.servShipID()));
                if(servoShip.getImpl() == null)return;
                servoControl(levelPos.pos(), servoShip, assemShip, logicalServoMotor);
                writePhysics(levelPos.pos(), servoShip, assemShip, logicalServoMotor);
            }
        );
    }
}
