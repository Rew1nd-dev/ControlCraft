package com.verr1.controlcraft.content.valkyrienskies.transform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.ClientShipTransformProvider;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;

public class LerpedTransformProvider implements ClientShipTransformProvider {

    public static LerpedTransformProvider replaceOrCreate(ClientShip ship){
        if(ship.getTransformProvider() instanceof LerpedTransformProvider){
            return (LerpedTransformProvider) ship.getTransformProvider();
        }else{
            LerpedTransformProvider prov = new LerpedTransformProvider();
            ship.setTransformProvider(prov);
            return prov;
        }
    }

    @Nullable
    @Override
    public ShipTransform provideNextRenderTransform(@NotNull ShipTransform prevTickTransform, @NotNull ShipTransform thisTickTransform, double v) {
        return  thisTickTransform; //null; //ShipTransformImpl.Companion.createFromSlerp(prevTickTransform, thisTickTransform, v); //thisTickTransform; //
    }

    @Nullable
    @Override
    public ShipTransform provideNextTransform(@NotNull ShipTransform shipTransform, @NotNull ShipTransform shipTransform1, @NotNull ShipTransform shipTransform2) {
        return null;
    }
}
