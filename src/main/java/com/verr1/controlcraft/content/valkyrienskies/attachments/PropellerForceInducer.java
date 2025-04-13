package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.data.logical.LogicalPropeller;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;

public class PropellerForceInducer extends ExpirableForceInducer<LogicalPropeller>{


    public static PropellerForceInducer getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(PropellerForceInducer.class);
        if(obj == null){
            obj = new PropellerForceInducer();
            ship.saveAttachment(PropellerForceInducer.class, obj);
        }
        return obj;
    }

    @Override
    protected void consume(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip, @NotNull LogicalPropeller context) {
        InducerControls.propellerTickControls(context, PhysShipWrapper.of(physShip));
    }
}
