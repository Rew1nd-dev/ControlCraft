package com.verr1.vscontrolcraft.compat.valkyrienskies.anchor;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.blocks.anchor.AnchorBlockEntity;
import com.verr1.vscontrolcraft.compat.valkyrienskies.base.AbstractExpirableForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.propeller.PropellerForceInducer;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import kotlin.jvm.functions.Function1;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.concurrent.ConcurrentHashMap;

public class AnchorForceInducer extends AbstractExpirableForceInducer {

    public static AnchorForceInducer getOrCreate(ServerShip ship){
        var obj = ship.getAttachment(AnchorForceInducer.class);
        if(obj == null) {
            obj = new AnchorForceInducer();
            ship.saveAttachment(AnchorForceInducer.class, obj);
        }
        return obj;
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

    }


    public void anchorControl(LevelPos anchorPos, PhysShipImpl physShip){
        if(!(VSMathUtils.getExisting(anchorPos) instanceof AnchorBlockEntity anchor))return;
        LogicalAnchor logicalAnchor = anchor.getLogicalAnchor();
        Vector3dc velocity = physShip.getPoseVel().getVel();
        Vector3dc fAirResistance = velocity.mul(-logicalAnchor.airResistance() * physShip.getInertia().getShipMass(), new Vector3d());
        Vector3dc fExtraGravity = new Vector3d(0, -physShip.getInertia().getShipMass(), 0).mul(logicalAnchor.extraGravity());


        double ts = 0.01667;
        int id = physShip.getTransform().getShipToWorldScaling().minComponent();
        double scale = physShip.getTransform().getShipToWorldScaling().get(id);
        double inertia = physShip.getInertia().getMomentOfInertiaTensor().m00();

        Vector3dc q_d = physShip.getPoseVel().getOmega();
        Vector3dc accel_d = new Vector3d(q_d.x(), q_d.y(), q_d.z()).mul(-2 / ts).mul(logicalAnchor.rotationalResistance());
        Vector3dc tRotationalResistance = new Vector3d(accel_d).mul(inertia * Math.pow(scale, 5));

        physShip.applyInvariantForce(fExtraGravity);
        physShip.applyInvariantForce(fAirResistance);
        physShip.applyInvariantTorque(tRotationalResistance);


    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        super.applyForces(physShip);
        getLives().forEach((k, v)->anchorControl(k, (PhysShipImpl) physShip));
    }
}
