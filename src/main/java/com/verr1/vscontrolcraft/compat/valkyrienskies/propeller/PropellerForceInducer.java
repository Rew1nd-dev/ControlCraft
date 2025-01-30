package com.verr1.vscontrolcraft.compat.valkyrienskies.propeller;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlockEntity;
import com.verr1.vscontrolcraft.compat.valkyrienskies.base.AbstractExpirableForceInducer;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;

@SuppressWarnings("Deprecated")
public class PropellerForceInducer extends AbstractExpirableForceInducer {


    public PropellerForceInducer() {
        super();
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
                       .mul(property.speed() * property.THRUST_RATIO())
                       .mul(property.canDrive() ? 1 : 0);

    }

    public Vector3d calcTorqueVector(LogicalPropeller property){
        return new Vector3d(property.direction())
                        .mul(property.speed() * property.TORQUE_RATIO())
                        .mul(property.reverseTorque() ? 1 : -1)
                        .mul(property.canDrive() ? 1 : 0);

    }

    public void applyPropellerControl(@NotNull PhysShip physShip, LevelPos controllerPos){
        if(!(VSMathUtils.getExisting(controllerPos) instanceof PropellerControllerBlockEntity controller))return;
        LogicalPropeller property = controller.getLogicalPropeller();
        if(property == null)return;

        Vector3d controllerCenterShipYard = Util.Vec3toVector3d(controllerPos.pos().getCenter());
        Vector3d shipCenterShipYard = (Vector3d) physShip.getTransform().getPositionInShip();
        Vector3d relativeRadius_ShipCoordinate = controllerCenterShipYard.sub(shipCenterShipYard);



        Vector3d torqueVector = calcTorqueVector(property);
        Vector3d forceVector = calcForceVector(property);
        Vector3d torque_WorldCoordinate = physShip.getTransform().getShipToWorld().transformDirection(torqueVector);
        Vector3d force_WorldCoordinate = physShip.getTransform().getShipToWorld().transformDirection(forceVector);

        physShip.applyInvariantForceToPos(force_WorldCoordinate, relativeRadius_ShipCoordinate);
        physShip.applyInvariantTorque(torque_WorldCoordinate);

    }


    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

    }



    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        super.applyForces(physShip);
        getLives().forEach((key, value) -> applyPropellerControl(physShip, key));
    }
}
