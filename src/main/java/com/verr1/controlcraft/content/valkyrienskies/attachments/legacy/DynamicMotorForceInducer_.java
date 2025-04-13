package com.verr1.controlcraft.content.valkyrienskies.attachments.legacy;

import com.verr1.controlcraft.content.blocks.motor.AbstractDynamicMotor;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.data.logical.LogicalDynamicMotor;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public final class DynamicMotorForceInducer_ extends AbstractExpirableForceInducer implements ShipForcesInducer {
    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        applyControlsWithOther(physShip, lookupPhysShip);
    }


    public static DynamicMotorForceInducer_ getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(DynamicMotorForceInducer_.class);
        if(obj == null){
            obj = new DynamicMotorForceInducer_();
            ship.saveAttachment(DynamicMotorForceInducer_.class, obj);
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
                            .getBlockEntityAt(pos.globalPos(), AbstractDynamicMotor.class)
                            .map(AbstractDynamicMotor::getLogicalMotor)
                            .filter(LogicalDynamicMotor::free)
                            .ifPresent(
                                logicalMotor -> InducerControls.dynamicMotorTickControls(
                                        logicalMotor,
                                        new PhysShipWrapper((PhysShipImpl) lookupPhysShip.invoke(logicalMotor.motorShipID())),
                                        new PhysShipWrapper((PhysShipImpl) physShip)
                                )
                            )
                );
    }

}
