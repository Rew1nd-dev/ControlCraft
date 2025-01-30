package com.verr1.vscontrolcraft.compat.valkyrienskies.magnet;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlockEntity;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetManager;
import com.verr1.vscontrolcraft.compat.valkyrienskies.base.AbstractExpirableForceInducer;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public class MagnetForceInducer extends AbstractExpirableForceInducer {

    public static MagnetForceInducer getOrCreate(ServerShip ship){
        MagnetForceInducer obj = ship.getAttachment(MagnetForceInducer.class);
        if(obj == null) {
            obj = new MagnetForceInducer();
            ship.saveAttachment(MagnetForceInducer.class, obj);
        }
        return obj;
    }

    public void writePhysicsAndApplyForces(@NotNull PhysShip physShip, LevelPos magnet){
        MagnetBlockEntity be = MagnetManager.getExisting(magnet);
        if(be == null)return;
        be.writePhysicsShipInfo(VSMathUtils.getShipPhysics((PhysShipImpl) physShip));
        LogicalMagnet logicalMagnet = be.getLogicalMagnet();
        if(logicalMagnet == null)return;
        Vector3d r_sc = be.getRelativePosition();
        Vector3d attraction = MagnetManager.calculateAttraction(logicalMagnet);
        physShip.applyInvariantForceToPos(attraction, r_sc);
    }


    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        tickAttraction(physShip);
        tickActivated();
    }


    public void tickAttraction(@NotNull PhysShip physShip){
        getLives().forEach((k, v) -> writePhysicsAndApplyForces(physShip, k));
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

    }

}
