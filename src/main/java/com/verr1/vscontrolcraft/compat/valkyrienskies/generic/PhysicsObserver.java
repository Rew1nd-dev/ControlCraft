package com.verr1.vscontrolcraft.compat.valkyrienskies.generic;

import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.valkyrienskies.propeller.PropellerForceInducer;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public class PhysicsObserver implements ShipForcesInducer {
    private ShipPhysics observation = null;

    public static PhysicsObserver getOrCreate(ServerShip ship){
        PhysicsObserver obj = ship.getAttachment(PhysicsObserver.class);
        if(obj == null) {
            obj = new PhysicsObserver();
            ship.saveAttachment(PhysicsObserver.class, obj);
        }
        return obj;
    }

    public ShipPhysics getObservation(){
        return observation;
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        observation = VSMathUtils.getShipPhysics((PhysShipImpl) physShip);
    }
}
