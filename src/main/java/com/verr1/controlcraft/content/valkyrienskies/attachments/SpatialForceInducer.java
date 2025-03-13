package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.blocks.slider.SliderBlockEntity;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.ServerBlockEntityGetter;
import com.verr1.controlcraft.foundation.data.logical.LogicalSlider;
import com.verr1.controlcraft.foundation.data.logical.LogicalSpatial;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.attachment.AttachmentHolder;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.Optional;

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


    public static SpatialForceInducer getOrCreate(AttachmentHolder ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(SpatialForceInducer.class);
        if(obj == null){
            obj = new SpatialForceInducer();
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
                                        .getBlockEntityAt(pos.globalPos(), SpatialAnchorBlockEntity.class)
                                        .map(SpatialAnchorBlockEntity::getLogicalSpatial)
                                        .filter(LogicalSpatial::shouldDrive)
                                        .ifPresent(
                                                logical -> InducerControls.spatialTickControls(logical, physShip)
                                        )
                );
    }
}
