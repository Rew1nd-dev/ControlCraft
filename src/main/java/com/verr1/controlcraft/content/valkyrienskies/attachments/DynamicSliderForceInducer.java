package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.data.logical.LogicalSlider;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;

public class DynamicSliderForceInducer extends ExpirableForceInducer<LogicalSlider>{

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
    protected void consume(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip, @NotNull LogicalSlider context) {
        InducerControls.sliderTickControls(
                context,
                PhysShipWrapper.of(lookupPhysShip.invoke(context.selfShipID())),
                PhysShipWrapper.of(physShip)
        );
    }
}
