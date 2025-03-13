package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.verr1.controlcraft.foundation.data.ShipHitResult;
import com.verr1.controlcraft.foundation.data.ShipPhysics;
import com.verr1.controlcraft.foundation.data.SynchronizedField;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.attachment.AttachmentHolder;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

public final class Observer implements ShipForcesInducer {

    private SynchronizedField<ShipPhysics> observedShip = new SynchronizedField<>(ShipPhysics.EMPTY);

    public static Observer getOrCreate(AttachmentHolder ship){
        var obj = ship.getAttachment(Observer.class);
        if(obj == null){
            obj = new Observer();
            ship.setAttachment(obj);
        }
        return obj;
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        observedShip.write(ShipPhysics.of(physShip));
    }

    public ShipPhysics read(){
        return observedShip.read();
    }
}
