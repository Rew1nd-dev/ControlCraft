package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.blocks.motor.AbstractKinematicMotor;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.Optional;

public final class KinematicMotorForceInducer extends AbstractExpirableForceInducer implements ShipForcesInducer {
    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        applyControlsWithOther(physShip, lookupPhysShip);
    }


    public static KinematicMotorForceInducer getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(KinematicMotorForceInducer.class);
        if(obj == null){
            obj = new KinematicMotorForceInducer();
            ship.saveAttachment(KinematicMotorForceInducer.class, obj);
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
                        .getBlockEntityAt(pos.globalPos(), AbstractKinematicMotor.class)
                        .map(AbstractKinematicMotor::getLogicalMotor)
                        .ifPresent(
                                logicalMotor -> InducerControls.kinematicMotorTickControls(
                                        logicalMotor,
                                        new PhysShipWrapper((PhysShipImpl) lookupPhysShip.invoke(logicalMotor.compShipID())),
                                        new PhysShipWrapper((PhysShipImpl) physShip)
                                )
                        )
            );


    }
}
