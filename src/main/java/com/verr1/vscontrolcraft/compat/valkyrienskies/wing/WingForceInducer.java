package com.verr1.vscontrolcraft.compat.valkyrienskies.wing;

import kotlin.jvm.functions.Function1;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.concurrent.ConcurrentHashMap;

public class WingForceInducer implements ShipForcesInducer {
    private ConcurrentHashMap<BlockPos, LogicalWing> anchorWings;
    private int lazyTickRate = 30;
    private int lazyTick = 0;


    public void updateLogicalWing(BlockPos controllerPos, LogicalWing wing){
        anchorWings.put(controllerPos, wing);
    }

    public void removeLogicalWing(BlockPos controllerPos){
        anchorWings.remove(controllerPos);
    }

    public static WingForceInducer getOrCreate(@NotNull ServerShip ship) {
        WingForceInducer obj = ship.getAttachment(WingForceInducer.class);
        if(obj == null) {
            obj = new WingForceInducer();
            ship.saveAttachment(WingForceInducer.class, obj);
        }
        return obj;
    }



    @Override
    public void applyForces(@NotNull PhysShip physShip) {

    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        ShipForcesInducer.super.applyForcesAndLookupPhysShips(physShip, lookupPhysShip);
    }
}
