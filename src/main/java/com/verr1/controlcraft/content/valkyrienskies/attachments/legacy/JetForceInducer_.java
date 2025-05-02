package com.verr1.controlcraft.content.valkyrienskies.attachments.legacy;

import com.verr1.controlcraft.content.blocks.jet.JetBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public final class JetForceInducer_ extends AbstractExpirableForceInducer implements ShipForcesInducer {
    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        applyControlsWithOther(physShip, lookupPhysShip);
    }


    public static JetForceInducer_ getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(JetForceInducer_.class);
        if(obj == null){
            obj = new JetForceInducer_();
            ship.saveAttachment(JetForceInducer_.class, obj);
        }
        return obj;

    }


    @Override
    public void applyControls(@NotNull PhysShip physShip) {
        getLives()
                .forEach(
                        (pos, live) ->
                                BlockEntityGetter
                                        .INSTANCE
                                        .getBlockEntityAt(pos.globalPos(), JetBlockEntity.class)
                                        .map(JetBlockEntity::getLogicalJet)
                                        .ifPresent(
                                                logicalJet -> InducerControls.jetTickControls(logicalJet, new PhysShipWrapper((PhysShipImpl) physShip))
                                        )
                );
    }

    @Override
    public void applyControlsWithOther(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

    }

}
