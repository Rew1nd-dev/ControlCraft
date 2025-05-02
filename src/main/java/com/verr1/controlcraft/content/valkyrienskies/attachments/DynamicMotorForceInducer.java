package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.data.logical.LogicalDynamicMotor;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;

public class DynamicMotorForceInducer extends ExpirableForceInducer<LogicalDynamicMotor>{


    public static DynamicMotorForceInducer getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(DynamicMotorForceInducer.class);
        if(obj == null){
            obj = new DynamicMotorForceInducer();
            ship.saveAttachment(DynamicMotorForceInducer.class, obj);
        }
        return obj;
    }

    @Override
    protected void consume(
            @NotNull PhysShip physShip,
            @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip,
            @NotNull LogicalDynamicMotor context
    ) {
        InducerControls.dynamicMotorTickControls(
                context,
                PhysShipWrapper.of(lookupPhysShip.invoke(context.motorShipID())),
                PhysShipWrapper.of(physShip)
        );
    }
}
