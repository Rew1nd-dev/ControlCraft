package com.verr1.controlcraft.content.valkyrienskies.transform;

import com.verr1.controlcraft.foundation.data.control.ImmutablePhysPose;
import com.verr1.controlcraft.foundation.vsapi.PhysPose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ServerShipTransformProvider;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;

public class KinematicMotorTransformProvider implements ServerShipTransformProvider {
    private PhysPose targetPose = ImmutablePhysPose.EMPTY;
    private int live = 10;
    private final int MAX_LIVE = 10;
    private long id = -1;

    public void set(@NotNull PhysPose pose){
        targetPose = pose;
        setAlive();
    }

    public KinematicMotorTransformProvider withID(long id){
        this.id = id;
        return this;
    }

    public static KinematicMotorTransformProvider replaceOrCreate(ServerShip ship){
        ship.setEnableKinematicVelocity(true);
        ship.setStatic(true);
        if(ship.getTransformProvider() instanceof KinematicMotorTransformProvider){
            return (KinematicMotorTransformProvider) ship.getTransformProvider();
        }else{
            KinematicMotorTransformProvider prov = new KinematicMotorTransformProvider().withID(ship.getId());
            ship.setTransformProvider(prov);
            return prov;
        }
    }

    private void setAlive(){
        live = MAX_LIVE;
    }

    private void tickLive(){
        if(live > 0)live--;
    }

    private boolean isAlive(){
        return live > 0;
    }

    @Nullable
    @Override
    public NextTransformAndVelocityData provideNextTransformAndVelocity(@NotNull ShipTransform prevTickTransform, @NotNull ShipTransform thisTickTransform) {
        tickLive();
        if(!isAlive())return null;
        return new NextTransformAndVelocityData(
                new ShipTransformImpl(
                        targetPose.getPos(),
                        thisTickTransform.getPositionInShip(),
                        targetPose.getRot(),
                        thisTickTransform.getShipToWorldScaling()
                ),
                new Vector3d(), // currently don't calculate this
                new Vector3d()
        );
    }



}
