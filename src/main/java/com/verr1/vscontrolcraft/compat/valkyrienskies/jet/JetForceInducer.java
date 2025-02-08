package com.verr1.vscontrolcraft.compat.valkyrienskies.jet;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.blocks.jet.JetBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.valkyrienskies.anchor.AnchorForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.base.AbstractExpirableForceInducer;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.logging.Level;

public class JetForceInducer extends AbstractExpirableForceInducer {
    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

    }

    public static JetForceInducer getOrCreate(ServerShip ship){
        var obj = ship.getAttachment(JetForceInducer.class);
        if(obj == null) {
            obj = new JetForceInducer();
            ship.saveAttachment(JetForceInducer.class, obj);
        }
        return obj;
    }

    public void jetControl(LevelPos pos, PhysShipImpl physShip){
        if(!(VSMathUtils.getExisting(pos) instanceof JetBlockEntity jet))return;
        jet.physics.write(VSMathUtils.getShipPhysics(physShip));

        LogicalJet logicalJet = jet.getLogicalJet();
        Vector3dc dir = logicalJet.direction();
        double thrust = logicalJet.thrust();

        Vector3dc force_sc = dir.mul(thrust, new Vector3d());
        Vector3dc force_wc = physShip.getTransform().getShipToWorld().transformDirection(force_sc, new Vector3d());

        Vector3dc ship_sc = physShip.getTransform().getPositionInShip();
        Vector3dc jet_sc = Util.Vec3toVector3d(pos.pos().getCenter());
        Vector3dc relativeRadius_sc = jet_sc.sub(ship_sc, new Vector3d());

        physShip.applyInvariantForceToPos(force_wc, relativeRadius_sc);

    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        super.applyForces(physShip);
        getLives().forEach((k, v)->jetControl(k, (PhysShipImpl) physShip));
    }
}
