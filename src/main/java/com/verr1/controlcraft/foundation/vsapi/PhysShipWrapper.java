package com.verr1.controlcraft.foundation.vsapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ChunkClaim;
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.PhysInertia;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.physics_api.PoseVel;

import java.util.Optional;

public record PhysShipWrapper(@Nullable PhysShipImpl impl) implements Ship {
    public PhysShipWrapper(@Nullable PhysShipImpl impl) {
        this.impl = impl;
    }

    public Optional<PhysShipImpl> implOptional() {
        return Optional.ofNullable(impl);
    }

    public double getMass() {
        return Optional
                .ofNullable(impl)
                .map(PhysShipImpl::get_inertia)
                .map(PhysInertia::getShipMass)
                .orElse(1.0);
    }

    public Matrix3dc getMomentOfInertia() {
        return Optional
                .ofNullable(impl)
                .map(PhysShipImpl::get_inertia)
                .map(PhysInertia::getMomentOfInertiaTensor)
                .orElse(new Matrix3d());
    }

    @Override
    public long getId() {
        return Optional
                .ofNullable(impl)
                .map(PhysShipImpl::getId)
                .orElse(-1L);
    }

    @Override
    public String getSlug() {
        return "wrapper";
    }


    public void applyInvariantForce(Vector3dc f){
        Optional.ofNullable(impl).ifPresent(p -> p.applyInvariantForce(f));
    }

    public void applyRotDependentInvariantForce(Vector3dc f){
        Optional.ofNullable(impl).ifPresent(p -> p.applyRotDependentForce(f));
    }

    public void applyInvariantTorque(Vector3dc t){
        Optional.ofNullable(impl).ifPresent(p -> p.applyInvariantTorque(t));
    }

    public void applyInvariantForceToPos(Vector3dc f, Vector3dc p){
        Optional.ofNullable(impl).ifPresent(s -> s.applyInvariantForceToPos(f, p));
    }

    public void applyRotDependentForceToPos(Vector3dc f, Vector3dc p){
        Optional.ofNullable(impl).ifPresent(s -> s.applyRotDependentForceToPos(f, p));
    }



    @NotNull
    @Override
    public ShipTransform getTransform() {
        return Optional
                .ofNullable(impl)
                .map(PhysShipImpl::getTransform)
                .orElse(ShipTransformImpl.Companion.createEmpty());
    }

    @NotNull
    @Override
    public ShipTransform getPrevTickTransform() {
        return getTransform();
    }

    @NotNull
    @Override
    public ChunkClaim getChunkClaim() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String getChunkClaimDimension() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChunkClaimDimension(@NotNull String s) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public AABBdc getWorldAABB() {
        return getTransform().createEmptyAABB();
    }

    @Nullable
    @Override
    public AABBic getShipAABB() {
        throw new UnsupportedOperationException();
    }

    private PoseVel getPoseVel() {
        return Optional
                .ofNullable(impl)
                .map(PhysShipImpl::getPoseVel)
                .orElse(PoseVel.Companion.createPoseVel(new Vector3d(), new Quaterniond()));
    }

    @NotNull
    @Override
    public Vector3dc getVelocity() {
        return getPoseVel().getVel();
    }

    @NotNull
    @Override
    public Vector3dc getOmega() {
        return getPoseVel().getOmega();
    }

    public Vector3dc getAngularVelocity() {
        return getPoseVel().getOmega();
    }

    @NotNull
    @Override
    public IShipActiveChunksSet getActiveChunksSet() {
        throw new UnsupportedOperationException();
    }


}
