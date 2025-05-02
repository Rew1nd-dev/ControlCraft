package com.verr1.controlcraft.content.valkyrienskies.attachments.legacy;

import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.data.logical.LogicalPropeller;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public final class PropellerForceInducer_ extends AbstractExpirableForceInducer implements ShipForcesInducer {
    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        applyControlsWithOther(physShip, lookupPhysShip);
    }


    public static PropellerForceInducer_ getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(PropellerForceInducer_.class);
        if(obj == null){
            obj = new PropellerForceInducer_();
            ship.saveAttachment(PropellerForceInducer_.class, obj);
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
                                        .getBlockEntityAt(pos.globalPos(), PropellerControllerBlockEntity.class)
                                        .map(PropellerControllerBlockEntity::getLogicalPropeller)
                                        .filter(LogicalPropeller::canDrive)
                                        .ifPresent(
                                                logical -> InducerControls.propellerTickControls(logical, new PhysShipWrapper((PhysShipImpl) physShip))
                                        )
                );
    }
}
