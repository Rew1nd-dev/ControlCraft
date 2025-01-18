package com.verr1.vscontrolcraft.compat.valkyrienskies.generic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ChunkClaim;
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;


// This is for math Util Only, Because You Can Pass This Class Just Like ServerShip
public class PhysShipWrapper implements Ship {
    private PhysShipImpl physShip;

    public PhysShipWrapper(PhysShipImpl physShip) {
        this.physShip = physShip;
    }

    public PhysShipImpl getImpl(){
        return physShip;
    }

    @Override
    public long getId() {
        return physShip.getId();
    }

    @Nullable
    @Override
    public String getSlug() {
        return "Not Implemented";
    }

    @NotNull
    @Override
    public ShipTransform getTransform() {
        return physShip.getTransform();
    }

    @NotNull
    @Override
    public ShipTransform getPrevTickTransform() {
        return physShip.getTransform();
    }

    @NotNull
    @Override
    public ChunkClaim getChunkClaim() {
        return null;
    }

    @NotNull
    @Override
    public String getChunkClaimDimension() {
        return "";
    }

    @Override
    public void setChunkClaimDimension(@NotNull String s) {

    }

    @NotNull
    @Override
    public AABBdc getWorldAABB() {
        return physShip.getTransform().createEmptyAABB();
    }

    @Nullable
    @Override
    public AABBic getShipAABB() {
        return null;
    }

    @NotNull
    @Override
    public Vector3dc getVelocity() {
        return physShip.getPoseVel().getVel();
    }

    @NotNull
    @Override
    public Vector3dc getOmega() {
        return physShip.getPoseVel().getOmega();
    }

    @NotNull
    @Override
    public IShipActiveChunksSet getActiveChunksSet() {
        return null;
    }
}
