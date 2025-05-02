package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.data.logical.LogicalAnchor;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;

public class AnchorForceInducer extends ExpirableForceInducer<LogicalAnchor>{

    public static AnchorForceInducer getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(AnchorForceInducer.class);
        if(obj == null){
            obj = new AnchorForceInducer();
            ship.saveAttachment(AnchorForceInducer.class, obj);
        }
        return obj;
    }

    @Override
    protected void consume(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip, @NotNull LogicalAnchor context) {
        InducerControls.anchorTickControls(
                context,
                PhysShipWrapper.of(physShip)
        );
    }
}
