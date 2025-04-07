package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.data.logical.LogicalSlider;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.Optional;

public final class DynamicSliderForceInducer extends AbstractExpirableForceInducer implements ShipForcesInducer {
    @Override
    public void applyForces(@NotNull org.valkyrienskies.core.api.ships.PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull org.valkyrienskies.core.api.ships.PhysShip physShip, @NotNull Function1<? super Long, ? extends org.valkyrienskies.core.api.ships.PhysShip> lookupPhysShip) {
        applyControlsWithOther(physShip, lookupPhysShip);
    }


    public static DynamicSliderForceInducer getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(DynamicSliderForceInducer.class);
        if(obj == null){
            obj = new DynamicSliderForceInducer();
            ship.saveAttachment(DynamicSliderForceInducer.class, obj);
        }
        return obj;

    }


    @Override
    public void applyControls(@NotNull org.valkyrienskies.core.api.ships.PhysShip physShip) {

    }

    @Override
    public void applyControlsWithOther(@NotNull org.valkyrienskies.core.api.ships.PhysShip physShip, @NotNull Function1<? super Long, ? extends org.valkyrienskies.core.api.ships.PhysShip> lookupPhysShip) {
        getLives()
            .forEach(
                (pos, live) ->
                    BlockEntityGetter
                        .INSTANCE
                        .getBlockEntityAt(pos.globalPos(), DynamicSliderBlockEntity.class)
                        .map(DynamicSliderBlockEntity::getLogicalSlider)
                        .filter(LogicalSlider::free)
                        .ifPresent(
                            logicalSlider -> InducerControls.sliderTickControls(
                                    logicalSlider,
                                    new PhysShipWrapper((PhysShipImpl) lookupPhysShip.invoke(logicalSlider.selfShipID())),
                                    new PhysShipWrapper((PhysShipImpl) physShip)
                            )
                        )
            );
    }
}
