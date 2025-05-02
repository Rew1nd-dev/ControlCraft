package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.data.logical.LogicalSpatial;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;

public class SpatialForceInducer extends ExpirableForceInducer<LogicalSpatial>{

    // getOrCreate

    public static SpatialForceInducer getOrCreate(ServerShip ship){
        var obj = ship.getAttachment(SpatialForceInducer.class);
        if(obj == null){
            obj = new SpatialForceInducer();
            ship.saveAttachment(SpatialForceInducer.class, obj);
        }
        return obj;
    }

    @Override
    protected void consume(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip, @NotNull LogicalSpatial context) {
        InducerControls.spatialTickControls(context, PhysShipWrapper.of(physShip));
    }
}
