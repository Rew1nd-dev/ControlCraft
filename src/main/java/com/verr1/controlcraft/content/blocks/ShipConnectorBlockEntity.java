package com.verr1.controlcraft.content.blocks;

import com.verr1.controlcraft.content.valkyrienskies.attachments.Observer;
import com.verr1.controlcraft.foundation.api.IConstraintHolder;
import com.verr1.controlcraft.foundation.data.constraint.ConstraintKey;
import com.verr1.controlcraft.foundation.data.ShipPhysics;
import com.verr1.controlcraft.foundation.managers.ConstraintCenter;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.apigame.joints.VSJoint;
import org.valkyrienskies.mod.api.ValkyrienSkies;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ShipConnectorBlockEntity extends OnShipBlockEntity
        implements IConstraintHolder
{



    private long companionShipID;
    private Direction companionShipDirection = Direction.UP;
    private final Map<String, ConstraintKey> registeredConstraintKeys = new HashMap<>();


    public ShipConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getCompanionShipID, this::setCompanionShipID, SerializeUtils.LONG, "companion"), Side.SERVER);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(
                () -> companionShipDirection.getSerializedName(),
                name -> companionShipDirection = Direction.valueOf(name.toUpperCase()),
                SerializeUtils.STRING,
                "companion direction"
        ), Side.SERVER);
    }

    public void registerConstraintKey(String id){
        registeredConstraintKeys.put(id, new ConstraintKey(getBlockPos(), getDimensionID(), id));
    }

    public @Nullable ConstraintKey getConstraintKey(String id){
        return registeredConstraintKeys.get(id);
    }

    public void overrideConstraint(String id, VSJoint newConstraint){
        Optional.ofNullable(getConstraintKey(id))
                .ifPresent(key -> ConstraintCenter.createOrReplaceNewConstrain(key, newConstraint));
    }

    public void removeConstraint(String id){
        Optional.ofNullable(getConstraintKey(id))
                .ifPresent(ConstraintCenter::removeConstraintIfPresent);
    }

    public @Nullable VSJoint getConstraint(String id){
        return Optional.ofNullable(getConstraintKey(id))
                .map(ConstraintCenter::get)
                .orElse(null);
    }


    protected void setCompanionShipID(long companionShipID) {
        this.companionShipID = companionShipID;
    }

    protected long getCompanionShipID() {
        return this.companionShipID;
    }

    public @Nullable LoadedServerShip getCompanionServerShip(){
        if(!(level instanceof ServerLevel lvl))return null;

        return Optional
                .ofNullable(ValkyrienSkies.getShipWorld(lvl.getServer()))
                .map(shipWorld -> shipWorld.getLoadedShips().getById(companionShipID))
                .orElse(null);
    }

    public void setCompanionShipDirection(@NotNull Direction direction){
        this.companionShipDirection = direction;
    }

    public Vector3d getCompanionShipDirectionJOML(){
        return ValkyrienSkies.set(new Vector3d(), getCompanionShipAlign().getNormal());
    }

    public  @NotNull Direction getCompanionShipAlign(){
        return companionShipDirection;
    }

    public boolean hasCompanionShip(){
        return getCompanionServerShip() != null;
    }


    public void clearCompanionShipInfo(){
        setCompanionShipID(-1);
        setCompanionShipDirection(Direction.UP);
    }

    @Override
    public abstract void destroyConstraints();


    @Override
    public void remove() {
        super.remove();
        if(level != null && !level.isClientSide){
            destroyConstraints();
        }
    }

    public @NotNull ShipPhysics readSelf(){
        if(level != null && level.isClientSide)return ShipPhysics.EMPTY;

        return Optional
                .ofNullable(getLoadedServerShip())
                .map(Observer::getOrCreate)
                .map(Observer::read)
                .orElse(ShipPhysics.EMPTY);
    }

    public @NotNull ShipPhysics readComp(){
        if(level != null && level.isClientSide)return ShipPhysics.EMPTY;

        return Optional
                .ofNullable(getCompanionServerShip())
                .map(Observer::getOrCreate)
                .map(Observer::read)
                .orElse(ShipPhysics.EMPTY);
    }

}
