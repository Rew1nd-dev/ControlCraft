package com.verr1.controlcraft.content.valkyrienskies.attachments;

import org.jetbrains.annotations.NotNull;
import org.joml.Random;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import static com.verr1.controlcraft.ControlCraft.RANDOM_GENERATOR;

public final class Caffeine implements ShipForcesInducer {

    public static Caffeine getOrCreate(ServerShip ship){
        var obj = ship.getAttachment(Caffeine.class);
        if(obj == null){
            obj = new Caffeine();
            ship.saveAttachment(Caffeine.class, obj);
        }
        return obj;

    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        double _01 = RANDOM_GENERATOR.nextFloat();
        double abs = 0.001;
        physShip.applyInvariantForce(new Vector3d(0, 2 * abs * (_01 - 0.5), 0));
    }
}
