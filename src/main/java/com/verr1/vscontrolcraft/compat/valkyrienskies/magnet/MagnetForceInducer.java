package com.verr1.vscontrolcraft.compat.valkyrienskies.magnet;

import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlockEntity;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetManager;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.concurrent.ConcurrentHashMap;

public class MagnetForceInducer implements ShipForcesInducer {
    private final ConcurrentHashMap<LogicalMagnet, Integer> magnets = new ConcurrentHashMap<>();
    private int TICKS_BEFORE_EXPIRED = 30;

    public static MagnetForceInducer getOrCreate(ServerShip ship){
        MagnetForceInducer obj = ship.getAttachment(MagnetForceInducer.class);
        if(obj == null) {
            obj = new MagnetForceInducer();
            ship.saveAttachment(MagnetForceInducer.class, obj);
        }
        return obj;
    }

    public void writePhysicsAndApplyForces(@NotNull PhysShip physShip, LogicalMagnet magnet){
        MagnetBlockEntity be = MagnetManager.getExisting(magnet);
        if(be == null)return;
        be.writePhysicsShipInfo(VSMathUtils.getShipPhysics((PhysShipImpl) physShip));
        Vector3d r_sc = be.getRelativePosition();
        Vector3d attraction = MagnetManager.calculateAttraction(magnet);
        physShip.applyInvariantForceToPos(attraction, r_sc);
    }

    public void activated(LogicalMagnet magnet){
        magnets.put(magnet, TICKS_BEFORE_EXPIRED);

    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        tickAttraction(physShip);
        tickActivated();
    }


    public void tickAttraction(@NotNull PhysShip physShip){
        magnets.forEach((k, v) -> writePhysicsAndApplyForces(physShip, k));
    }

    public void tickActivated(){
        magnets.entrySet().forEach(e -> e.setValue(e.getValue() - 1));
        magnets.entrySet().removeIf(e -> e.getValue() < 0);
    }
}
