package com.verr1.controlcraft.foundation.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBi;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ChunkClaim;
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;

public class GroundBodyShip implements Ship {
    public static final ShipTransform EMPTY_TRANSFORM = ShipTransformImpl.Companion.createEmpty();


    @Override
    public long getId() {
        return -1;
    }

    @Nullable
    @Override
    public String getSlug() {
        return "ControlCraft$GroundBody";
    }

    @NotNull
    @Override
    public ShipTransform getTransform() {
        return EMPTY_TRANSFORM;
    }

    @NotNull
    @Override
    public ShipTransform getPrevTickTransform() {
        return EMPTY_TRANSFORM;
    }

    @NotNull
    @Override
    public ChunkClaim getChunkClaim() {
        throw new UnsupportedOperationException("GroundBodyShip does not have chunk claim, How is this method called ??");
    }

    @NotNull
    @Override
    public String getChunkClaimDimension() {
        throw new UnsupportedOperationException("GroundBodyShip does not have chunk claim dimension, How is this method called ??");
    }

    @Override
    public void setChunkClaimDimension(@NotNull String s) {
        throw new UnsupportedOperationException("GroundBodyShip does not have chunk claim dimension, How is this method called ??");
    }

    @NotNull
    @Override
    public AABBdc getWorldAABB() {
        return new AABBd();
    }

    @Nullable
    @Override
    public AABBic getShipAABB() {
        return new AABBi();
    }

    @NotNull
    @Override
    public Vector3dc getVelocity() {
        return new Vector3d();
    }

    @NotNull
    @Override
    public Vector3dc getOmega() {
        return new Vector3d();
    }

    @NotNull
    @Override
    public IShipActiveChunksSet getActiveChunksSet() {
        throw new UnsupportedOperationException("GroundBodyShip does not have active chunk set, How is this method called ??");
    }
}
