package com.verr1.vscontrolcraft.compat.valkyrienskies.servo;

import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
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
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.concurrent.ConcurrentHashMap;
//TODO: remove invalid servo
public class ServoMotorForceInducer implements ShipForcesInducer {
    private final int lazyTickRate = 10;
    private int lazyTickCount = lazyTickRate;
    private int TICKS_BEFORE_EXPIRED = 3;
    private final ConcurrentHashMap<BlockPos, LogicalServoMotor> servoProperties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BlockPos, Integer> servoLife = new ConcurrentHashMap<>();

    public static ServoMotorForceInducer getOrCreate(@NotNull ServerShip ship){
        ServoMotorForceInducer obj = ship.getAttachment(ServoMotorForceInducer.class);
        if(obj == null){
            obj = new ServoMotorForceInducer();
            ship.saveAttachment(ServoMotorForceInducer.class, obj);
        }
        return obj;
    }

    public void servoControl(BlockPos servoPos, PhysShipWrapper servoShip, PhysShipWrapper assemShip, LogicalServoMotor property){
        BlockEntity be = property.level().getExistingBlockEntity(servoPos);
        if(!(be instanceof AbstractServoMotor servo))return;
        double angle = VSMathUtils.get_xc2yc(servoShip, assemShip, property.servoDir(), property.assemDir());
        // servo.debug_angle_accessor = angle;

        double accel_scale = servo.getControllerInfoHolder().calculateControlValueScaleAngular();
        double control_torque = property.torqueCallBack().get();
        double internal_torque = assemShip.getImpl().getInertia().getMomentOfInertiaTensor().m00() * accel_scale;
        Vector3dc direction = Util.Vec3itoVector3d(property.servoDir().getNormal());

        Vector3dc controlTorque_sc = direction.mul((-control_torque + internal_torque) * -1  , new Vector3d()); //
        Vector3dc controlTorque_wc = VSMathUtils.get_sc2wc(servoShip).transform(controlTorque_sc, new Vector3d());
        servo.getControllerInfoHolder().overrideError(angle);

        assemShip.getImpl().applyInvariantTorque(controlTorque_wc);
        servoShip.getImpl().applyInvariantTorque(controlTorque_wc.mul(-1, new Vector3d()));
    }

    public void writePhysics(BlockPos servoPos, PhysShipWrapper servoShip, PhysShipWrapper assemShip, LogicalServoMotor property){
        BlockEntity be = property.level().getExistingBlockEntity(servoPos);
        if(!(be instanceof AbstractServoMotor servo))return;
        servo.ownPhysics.write(VSMathUtils.getShipPhysics(servoShip.getImpl()));
        servo.asmPhysics.write(VSMathUtils.getShipPhysics(assemShip.getImpl()));
    }

    public void updateLogicalServoMotor(BlockPos pos, LogicalServoMotor property){
        servoProperties.put(pos, property);
        servoLife.put(pos, TICKS_BEFORE_EXPIRED);
    }

    public void tickActivated(){
        servoLife.entrySet().forEach(e -> e.setValue(e.getValue() - 1));
        servoProperties.entrySet().removeIf(e -> servoLife.get(e.getKey()) != null && servoLife.get(e.getKey()) < 0);
        servoLife.entrySet().removeIf(e -> e.getValue() < 0);
    }

    public void lazyTick(){
        tickActivated();
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickCount--;
        if(lazyTickCount <= 0){
            lazyTickCount = lazyTickRate;
            lazyTick();
        }
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        ShipForcesInducer.super.applyForcesAndLookupPhysShips(physShip, lookupPhysShip);
        PhysShipWrapper assemShip = new PhysShipWrapper((PhysShipImpl) physShip);
        servoProperties.forEach(
            (blockPos, logicalServoMotor) -> {
                PhysShipWrapper servoShip = new PhysShipWrapper((PhysShipImpl) lookupPhysShip.invoke(logicalServoMotor.servoShipID()));
                if(servoShip.getImpl() == null)return;
                servoControl(blockPos, servoShip, assemShip, logicalServoMotor);
                writePhysics(blockPos, servoShip, assemShip, logicalServoMotor);
            }
        );
    }
}
