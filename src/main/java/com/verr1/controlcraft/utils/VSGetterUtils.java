package com.verr1.controlcraft.utils;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nullable;
import java.util.Optional;

public class VSGetterUtils {

    public static Optional<LoadedServerShip> getShip(ServerLevel level, BlockPos pos){
        return getShipOn(level, pos)
                .map(Ship::getId)
                .flatMap(
                        id -> Optional
                                .ofNullable(ValkyrienSkies.getShipWorld(level.getServer()))
                                .map((shipWorld -> shipWorld.getLoadedShips().getById(id)))
                );
    }

    public static Optional<LoadedServerShip> getShip(WorldBlockPos pos){
        if(ControlCraftServer.INSTANCE == null)return Optional.empty();
        MinecraftServer server = ControlCraftServer.INSTANCE;
        return getShip(server.getLevel(pos.globalPos().dimension()), pos.pos());
    }


    public static Optional<Ship> getShipOn(Level level, BlockPos pos){
        return Optional.ofNullable(ValkyrienSkies.getShipManagingBlock(level, pos));
    }


    public static Quaterniondc getQuaternion(WorldBlockPos pos){
        return getShip(pos).map(ship -> ship.getTransform().getRotation()).orElse(new Quaterniond());
    }

    public static Vector3dc getAbsolutePosition(WorldBlockPos pos){
        Vector3d worldPos = ValkyrienSkies.set(new Vector3d(), pos.pos().getCenter());
        return getShip(pos)
                .map(ship -> ship
                        .getShipToWorld()
                        .transformPosition(worldPos)
                ).orElse(worldPos);
    }

    public static Vector3dc getAbsoluteFacePosition(WorldBlockPos pos, Direction face){
        Vector3d dir = ValkyrienSkies.set(new Vector3d(), face.getNormal());
        Vector3d worldPos = ValkyrienSkies.set(new Vector3d(), pos.pos()).fma(0.5, dir);
        return getShip(pos)
                .map(ship -> ship
                        .getShipToWorld()
                        .transformPosition(worldPos)
                ).orElse(worldPos);
    }

    public static boolean isOnSameShip(WorldBlockPos pos1, WorldBlockPos pos2){
        return getShip(pos1).map(ship -> ship.getId() == getShip(pos2).map(Ship::getId).orElse(-1L)).orElse(false);

    }

}
