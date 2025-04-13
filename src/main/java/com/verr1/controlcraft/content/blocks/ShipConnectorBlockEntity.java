package com.verr1.controlcraft.content.blocks;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.content.valkyrienskies.attachments.Observer;
import com.verr1.controlcraft.foundation.api.IConstraintHolder;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.constraint.ConstraintKey;
import com.verr1.controlcraft.foundation.data.ShipPhysics;
import com.verr1.controlcraft.foundation.managers.ConstraintCenter;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class ShipConnectorBlockEntity extends OnShipBlockEntity
        implements IConstraintHolder
{
    private long companionShipID;
    private Direction companionShipDirection = Direction.UP;



    private BlockPos blockConnectContext = BlockPos.ZERO;
    private final Map<String, ConstraintKey> registeredConstraintKeys = new HashMap<>();

    public static final NetworkKey COMPANION = NetworkKey.create("companion");
    public static final NetworkKey COMPANION_DIRECTION = NetworkKey.create("companion_direction");
    public static final NetworkKey BLOCK_CONNECT_CONTEXT = NetworkKey.create("block_connect_context");

    public ShipConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        buildRegistry(COMPANION).withBasic(SerializePort.of(this::getCompanionShipID, this::setCompanionShipID, SerializeUtils.LONG)).dispatchToSync().register();
        buildRegistry(COMPANION_DIRECTION).withBasic(SerializePort.of(() -> companionShipDirection, d -> companionShipDirection = d, SerializeUtils.ofEnum(Direction.class))).dispatchToSync().register();
        buildRegistry(BLOCK_CONNECT_CONTEXT).withBasic(SerializePort.of(
                () -> blockConnectContext().asLong(),
                l -> setBlockConnectContext(BlockPos.of(l)),
                SerializeUtils.LONG)
        ).register();

        /*
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getCompanionShipID, this::setCompanionShipID, SerializeUtils.LONG, COMPANION), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(
                () -> companionShipDirection.getSerializedName(),
                name -> companionShipDirection = Direction.valueOf(name.toUpperCase()),
                SerializeUtils.STRING,
                COMPANION_DIRECTION
        ), Side.SHARED);
        * */


    }

    public BlockPos blockConnectContext() {
        return blockConnectContext;
    }

    public void setBlockConnectContext(BlockPos blockConnectContext) {
        ControlCraft.LOGGER.info("setBlockConnectContext: " + blockConnectContext);
        this.blockConnectContext = blockConnectContext;
    }

    public void registerConstraintKey(String id){
        registeredConstraintKeys.put(id, new ConstraintKey(getBlockPos(), getDimensionID(), id));
    }

    public @Nullable ConstraintKey getConstraintKey(String id){
        return registeredConstraintKeys.get(id);
    }

    public void overrideConstraint(String id, VSConstraint newConstraint){
        Optional.ofNullable(getConstraintKey(id))
                .ifPresent(key -> ConstraintCenter.createOrReplaceNewConstrain(key, newConstraint));
    }

    public void updateConstraint(String id, VSConstraint newConstraint){
        Optional.ofNullable(getConstraintKey(id))
                .ifPresent(key -> ConstraintCenter.updateOrCreateConstraint(key, newConstraint));
    }

    public void removeConstraint(String id){
        Optional.ofNullable(getConstraintKey(id))
                .ifPresent(ConstraintCenter::removeConstraintIfPresent);
    }

    public @Nullable VSConstraint getConstraint(String id){
        return Optional.ofNullable(getConstraintKey(id))
                .map(ConstraintCenter::get)
                .orElse(null);
    }


    protected void setCompanionShipID(long companionShipID) {
        this.companionShipID = companionShipID;
        if(level != null && level.isClientSide)return;
        syncForAllPlayers(true, COMPANION);
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

    public @Nullable ClientShip getCompanionClientShip(){
        if(!(level instanceof ClientLevel lvl))return null;

        return Optional
                .ofNullable(ValkyrienSkies.getShipWorld(lvl))
                .map(shipWorld -> shipWorld.getLoadedShips().getById(companionShipID))
                .orElse(null);
    }

    public void setCompanionShipDirection(@NotNull Direction direction){
        this.companionShipDirection = direction;
        if(level != null && level.isClientSide)return;
        syncForAllPlayers(true, COMPANION_DIRECTION);
    }

    public Vector3d getCompanionShipAlignJOML(){
        return ValkyrienSkies.set(new Vector3d(), getCompanionShipAlign().getNormal());
    }

    public  @NotNull Direction getCompanionShipAlign(){
        return companionShipDirection;
    }

    public boolean noCompanionShip(){
        return getCompanionServerShip() == null;
    }


    public void clearCompanionShipInfo(){
        setCompanionShipID(-1);
        setCompanionShipDirection(Direction.UP);
        setBlockConnectContext(BlockPos.ZERO);
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



    public @NotNull ShipPhysics readComp(){
        if(level != null && level.isClientSide)return ShipPhysics.EMPTY;

        return Optional
                .ofNullable(getCompanionServerShip())
                .map(Observer::getOrCreate)
                .map(Observer::read)
                .orElseGet(() -> ShipPhysics.of(getCompanionServerShip()));
    }



}
