package com.verr1.controlcraft.utils;

import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nullable;
import java.util.Optional;

public class VSGetterUtils {

    public static Optional<LoadedServerShip> getLoadedServerShip(@NotNull ServerLevel level, BlockPos pos){
        return Optional.ofNullable(VSGameUtilsKt.getShipObjectManagingPos(level, pos));
    }

    @OnlyIn(Dist.CLIENT)
    public static Optional<ClientShip> getClientShip(BlockPos pos){
        return Optional
                .ofNullable(Minecraft.getInstance().level)
                .map(lvl -> ValkyrienSkies.getShipManagingBlock(lvl, pos))
                .filter(ClientShip.class::isInstance)
                .map(ClientShip.class::cast);
    }

    public static Optional<LoadedServerShip> getLoadedServerShip(WorldBlockPos pos){
        return Optional.ofNullable(ControlCraftServer.INSTANCE).flatMap(
                s -> Optional.ofNullable(
                        pos.level(s)
                ).flatMap(lvl -> getLoadedServerShip(lvl, pos.pos()))
        );
    }


    public static Optional<Ship> getShip(Level level, BlockPos pos){
        return Optional.ofNullable(ValkyrienSkies.getShipManagingBlock(level, pos));
    }


    public static Quaterniondc getQuaternion(WorldBlockPos pos){
        return getLoadedServerShip(pos).map(ship -> ship.getTransform().getShipToWorldRotation()).orElse(new Quaterniond());
    }

    public static Optional<Ship> getShip(WorldBlockPos pos){
        return Optional.ofNullable(
            ControlCraftServer.INSTANCE == null ?
                getClientShip(pos.pos()).map(Ship.class::cast).orElse(null) :
                getLoadedServerShip(pos).map(Ship.class::cast).orElse(null)
        );
    }

    public static Vector3d getAbsolutePosition(WorldBlockPos pos){
        Vector3d worldPos = ValkyrienSkies.toJOML(pos.pos().getCenter());

        Ship ship = ControlCraftServer.INSTANCE == null ?
                getClientShip(pos.pos()).orElse(null) :
                getLoadedServerShip(pos).orElse(null);

        return Optional.ofNullable(ship)
                .map(s -> s.getShipToWorld().transformPosition(worldPos))
                .orElse(worldPos);
    }

    public static Vector3d getAbsoluteFacePosition(WorldBlockPos pos, Direction face){
        Vector3d dir = ValkyrienSkies.set(new Vector3d(), face.getNormal());
        Vector3d worldPos = ValkyrienSkies.set(new Vector3d(), pos.pos()).fma(0.5, dir);
        return getShip(pos)
                .map(ship -> ship
                        .getShipToWorld()
                        .transformPosition(worldPos)
                ).orElse(worldPos);
    }

    public static boolean isOnSameShip(WorldBlockPos pos1, WorldBlockPos pos2){
        return getLoadedServerShip(pos1).map(ship -> ship.getId() == getLoadedServerShip(pos2).map(Ship::getId).orElse(-1L)).orElse(false);

    }

    public static @Nullable ServerShipWorldCore getServerShipWorldCore(){
        return Optional
                .ofNullable(ValkyrienSkies.getShipWorld(ControlCraftServer.INSTANCE))
                .filter(ServerShipWorldCore.class::isInstance)
                .map(ServerShipWorldCore.class::cast).orElse(null);
    }

}
