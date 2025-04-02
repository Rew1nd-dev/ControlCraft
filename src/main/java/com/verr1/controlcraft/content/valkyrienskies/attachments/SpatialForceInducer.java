package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.data.logical.LogicalSpatial;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public final class SpatialForceInducer extends AbstractExpirableForceInducer implements ShipForcesInducer {
    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        applyControlsWithOther(physShip, lookupPhysShip);
    }


    public static SpatialForceInducer getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(SpatialForceInducer.class);
        if(obj == null){
            obj = new SpatialForceInducer();
            ship.saveAttachment(SpatialForceInducer.class, obj);
        }
        return obj;

    }


    @Override
    public void applyControls(@NotNull PhysShip physShip) {

    }

    @Override
    public void applyControlsWithOther(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        getLives()
                .forEach(
                        (pos, live) ->
                                BlockEntityGetter
                                        .INSTANCE
                                        .getBlockEntityAt(pos.globalPos(), SpatialAnchorBlockEntity.class)
                                        .map(SpatialAnchorBlockEntity::getLogicalSpatial)
                                        .filter(LogicalSpatial::shouldDrive)
                                        .ifPresent(
                                                logical -> InducerControls.spatialTickControls(logical, new PhysShipWrapper((PhysShipImpl) physShip))
                                        )
                );
    }
}
