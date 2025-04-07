package com.verr1.controlcraft.content.blocks;

import com.verr1.controlcraft.content.valkyrienskies.attachments.Observer;
import com.verr1.controlcraft.foundation.data.ShipPhysics;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;

import javax.annotation.Nullable;
import java.util.*;

import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toJOML;

public abstract class OnShipBlockEntity extends NetworkBlockEntity
{
    public OnShipBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Vector3d getDirectionJOML() {
        return ValkyrienSkies.set(new Vector3d(), getDirection().getNormal());
    }

    public @NotNull Direction getDirection(){
        if(getBlockState().hasProperty(BlockStateProperties.FACING)) return getBlockState().getValue(BlockStateProperties.FACING);
        return Direction.UP;
    }



    public @NotNull ShipPhysics readSelf(){
        if(level == null || level.isClientSide)return ShipPhysics.EMPTY;

        return Optional
                .ofNullable(getLoadedServerShip())
                .filter(s -> !s.isStatic())
                .map(Observer::getOrCreate)
                .map(Observer::read)
                .orElseGet(() -> ShipPhysics.of(getLoadedServerShip()));
    }

    public boolean isOnShip(){
        return getShipOn() != null;
    }

    public @Nullable LoadedServerShip getLoadedServerShip(){
        if(level == null || level.isClientSide)return null;
        return Optional
                .of(ValkyrienSkies.getShipWorld(level.getServer()))
                .map((shipWorld -> shipWorld.getLoadedShips().getById(getShipOrGroundID()))).orElse(null);
    }

    @OnlyIn(Dist.CLIENT)
    public @Nullable ClientShip getClientShip(){
        if(level == null || !level.isClientSide)return null;
        return Optional
                .of(ValkyrienSkies.getShipWorld(Minecraft.getInstance()))
                .map(shipWorld -> shipWorld.getLoadedShips().getById(getShipOrGroundID())).orElse(null);
    }

    public @Nullable Ship getShipOn(){
        return ValkyrienSkies.getShipManagingBlock(level, getBlockPos());
    }

    public Quaterniondc getSelfShipQuaternion(){
        Quaterniond q = new Quaterniond();
        Optional
            .ofNullable(getShipOn())
            .ifPresent(
                    serverShip -> serverShip
                            .getTransform()
                            .getShipToWorldRotation()
                            .get(q)
            );
        return q;
    }




    public String getDimensionID(){
        return Optional
                .ofNullable(level)
                .map(ValkyrienSkies::getDimensionId)
                .orElse("");
    }



    public long getGroundBodyID(){
        return Optional
                .ofNullable(level)
                .filter(ServerLevel.class::isInstance)
                .map(ServerLevel.class::cast)
                .map(ValkyrienSkies::getShipWorld)
                .map(ServerShipWorldCore::getDimensionToGroundBodyIdImmutable)
                .map(m -> m.get(getDimensionID()))
                .orElse(-1L);
    }

    public long getShipOrGroundID(){
        return Optional
                .ofNullable(getShipOn())
                .map(Ship::getId)
                .orElse(getGroundBodyID());

    }



}
