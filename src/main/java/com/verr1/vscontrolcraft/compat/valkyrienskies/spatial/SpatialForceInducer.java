package com.verr1.vscontrolcraft.compat.valkyrienskies.spatial;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialAnchorBlockEntity;
import com.verr1.vscontrolcraft.compat.valkyrienskies.base.AbstractExpirableForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.generic.PhysShipWrapper;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import kotlin.jvm.functions.Function1;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public class SpatialForceInducer extends AbstractExpirableForceInducer {


    public static SpatialForceInducer getOrCreate(@NotNull ServerShip ship){
        var obj = ship.getAttachment(SpatialForceInducer.class);
        if(obj == null){
            obj = new SpatialForceInducer();
            ship.saveAttachment(SpatialForceInducer.class, obj);
        }
        return obj;
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

    }

    public void spatialControl(LevelPos pos ,PhysShipWrapper ship){

        BlockEntity be = VSMathUtils.getExisting(pos);
        if(!(be instanceof SpatialAnchorBlockEntity spatial))return;
        LogicalSpatial property = spatial.getLogicalSpatial();
        if(!property.shouldDrive())return;

        spatial.getSchedule().overridePhysics(ship.getImpl());
        Vector3dc controlTorque = spatial.getSchedule().calcControlTorque();
        Vector3dc controlForce  = spatial.getSchedule().calcControlForce();

        ship.getImpl().applyInvariantForce(controlForce);
        ship.getImpl().applyInvariantTorque(controlTorque);


    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        super.applyForces(physShip);
        getLives().forEach((k, v)->spatialControl(k, new PhysShipWrapper((PhysShipImpl) physShip)));
    }
}
