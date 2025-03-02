package com.verr1.vscontrolcraft.mixin.accessor;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;

import java.util.Map;
import java.util.Set;

@Mixin(ShipObjectServerWorld.class)
public interface ShipObjectServerWorldAccessor {


    @Accessor(value = "constraints", remap = false)
    Map<Integer, VSConstraint> controlCraft$getConstraints();

    @Accessor(value = "shipIdToConstraints", remap = false)
    Map<Long, Set<Integer>> controlCraft$getShipIdToConstraints();


}
