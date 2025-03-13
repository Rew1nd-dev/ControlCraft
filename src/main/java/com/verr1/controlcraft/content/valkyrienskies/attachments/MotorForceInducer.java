package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.blocks.motor.AbstractMotorBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.ServerBlockEntityGetter;
import com.verr1.controlcraft.foundation.data.logical.LogicalMotor;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.attachment.AttachmentHolder;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.Optional;

public final class MotorForceInducer extends AbstractExpirableForceInducer implements ShipForcesInducer {
    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        applyControlsWithOther(physShip, lookupPhysShip);
    }


    public static MotorForceInducer getOrCreate(AttachmentHolder ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(MotorForceInducer.class);
        if(obj == null){
            obj = new MotorForceInducer();
            ship.setAttachment(obj);
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
                    ServerBlockEntityGetter
                            .INSTANCE
                            .getBlockEntityAt(pos.globalPos(), AbstractMotorBlockEntity.class)
                            .map(AbstractMotorBlockEntity::getLogicalMotor)
                            .filter(LogicalMotor::free)
                            .ifPresent(
                                logicalMotor -> Optional
                                        .ofNullable(lookupPhysShip.invoke(logicalMotor.compShipID()))
                                        .ifPresent(compShip -> InducerControls.motorTickControls(logicalMotor, physShip, compShip))
                            )
                );
    }

}
