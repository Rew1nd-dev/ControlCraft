package com.verr1.vscontrolcraft.compat.valkyrienskies.propeller;

import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlockEntity;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("Deprecated")
public class PropellerForceInducer implements ShipForcesInducer {

    private final int lazyTickRate = 30;
    private int lazyTickCount = lazyTickRate;
    private final ConcurrentHashMap<BlockPos, LogicalPropeller> controllerProperties = new ConcurrentHashMap<>();

    public PropellerForceInducer() {
        super();
    }

    public void updateLogicalPropeller(BlockPos controllerPos, LogicalPropeller property){
        controllerProperties.put(controllerPos, property);
    }

    public void removeLogicalPropeller(BlockPos controllerPos){
        controllerProperties.remove(controllerPos);
    }


    public static PropellerForceInducer getOrCreate(@NotNull ServerShip ship) {
        PropellerForceInducer obj = ship.getAttachment(PropellerForceInducer.class);
        if(obj == null) {
            obj = new PropellerForceInducer();
            ship.saveAttachment(PropellerForceInducer.class, obj);
        }
        return obj;
    }

    public Vector3d calcForceVector(LogicalPropeller property){
        return new Vector3d(property.direction())
                       .mul(property.speedCallBack().get() * property.THRUST_RATIO())
                       .mul(property.canDrive() ? 1 : 0);

    }

    public Vector3d calcTorqueVector(LogicalPropeller property){
        return new Vector3d(property.direction())
                        .mul(property.speedCallBack().get() * property.TORQUE_RATIO())
                        .mul(property.reverseTorque() ? 1 : -1)
                        .mul(property.canDrive() ? 1 : 0);

    }

    public void applyPropellerControl(@NotNull PhysShip physShip, BlockPos controllerPos, LogicalPropeller property){
        Vector3d controllerCenterShipYard = Util.Vec3toVector3d(controllerPos.getCenter());
        Vector3d shipCenterShipYard = (Vector3d) physShip.getTransform().getPositionInShip();
        Vector3d relativeRadius_ShipCoordinate = controllerCenterShipYard.sub(shipCenterShipYard);
        Vector3d torqueVector = calcTorqueVector(property);
        Vector3d forceVector = calcForceVector(property);
        Vector3d torque_WorldCoordinate = physShip.getTransform().getShipToWorld().transformDirection(torqueVector);
        Vector3d force_WorldCoordinate = physShip.getTransform().getShipToWorld().transformDirection(forceVector);

        physShip.applyInvariantForceToPos(force_WorldCoordinate, relativeRadius_ShipCoordinate);
        physShip.applyInvariantTorque(torque_WorldCoordinate);

    }

    public void removeInvalidControllers(){
        controllerProperties.entrySet().removeIf(
                entry -> !(entry
                        .getValue()
                        .serverLevel()
                        .getBlockEntity(entry.getKey()) instanceof PropellerControllerBlockEntity
                )
        );
    }

    public void lazyTick(){
        if(lazyTickCount-- > 0)return;
        lazyTickCount = lazyTickRate;
        removeInvalidControllers();
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        controllerProperties.forEach(
                (blockPos, property) -> applyPropellerControl(physShip, blockPos, property)
        );
        lazyTick();
    }
}
