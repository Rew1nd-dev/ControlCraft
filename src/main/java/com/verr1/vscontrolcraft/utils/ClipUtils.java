package com.verr1.vscontrolcraft.utils;

import com.verr1.vscontrolcraft.base.DataStructure.ShipHitResult;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ClipUtils {

    public static @Nullable EntityHitResult clipEntity(
            Vec3 from,
            Vec3 to,
            AABB aabb,
            double coneRatio,
            Level level,
            Predicate<Entity> filter
    ){
        double maxDistance = to.subtract(from).lengthSqr();
        List<Entity> list = level.getEntities(null, aabb).stream().filter(filter).toList();
        for(var candidate : list){
            double d = from.distanceTo(new Vec3(candidate.getX(), candidate.getY(), candidate.getZ()));
            Optional<Vec3> clip0 = candidate.getBoundingBox().inflate(Math.min(d, 1) * coneRatio).clip(from, to);
            if(clip0.isEmpty())continue;
            Vec3 clip = clip0.get();
            if(clip.distanceTo(from) > maxDistance)continue;
            return new EntityHitResult(candidate, clip);
        }
        return null;
    }

    public static @Nullable ShipHitResult clipShip(
            Vec3 from,
            Vec3 to,
            AABB aabb,
            double coneRatio,
            Level level,
            Predicate<Ship> filter
    ){
        double maxDistance = to.subtract(from).lengthSqr();
        List<LoadedShip> list = VSGameUtilsKt
                .getShipObjectWorld(level)
                .getLoadedShips()
                .stream()
                .filter(filter)
                .filter(s -> s.getWorldAABB().intersectsAABB(VectorConversionsMCKt.toJOML(aabb)))
                .toList();

        for(var candidate : list){
            double d = from.distanceTo(VectorConversionsMCKt.toMinecraft(candidate.getTransform().getPositionInWorld()));
            Optional<Vec3> clip0 = VectorConversionsMCKt.toMinecraft(candidate.getWorldAABB()).inflate(Math.min(d, 1) * coneRatio).clip(from, to);
            if(clip0.isEmpty())continue;
            Vec3 clip = clip0.get();
            if(clip.distanceTo(from) > maxDistance)continue;
            return new ShipHitResult(clip, candidate);
        }
        return null;
    }

    public static @Nullable EntityHitResult clipClientPlayer(
            Vec3 from,
            Vec3 to,
            AABB aabb,
            double coneRatio,
            ClientLevel level,
            Predicate<AbstractClientPlayer> filter
    ){
        double maxDistance = to.subtract(from).lengthSqr();
        List<AbstractClientPlayer> list = level.players();
        // Not implemented
        return null;
    }

    public static @Nullable EntityHitResult clipServerPlayer(
            Vec3 from,
            Vec3 to,
            AABB aabb,
            double coneRatio,
            ServerLevel level,
            Predicate<ServerPlayer> filter
    ){
        double maxDistance = to.subtract(from).lengthSqr();
        List<ServerPlayer> list = level
                .players()
                .stream()
                .filter(filter)
                .filter(p -> p.getBoundingBox().intersects(aabb))
                .toList();
        for(var candidate : list){
            double d = from.distanceTo(new Vec3(candidate.getX(), candidate.getY(), candidate.getZ()));
            Optional<Vec3> clip0 = candidate.getBoundingBox().inflate(Math.min(d, 1) * coneRatio).clip(from, to);
            if(clip0.isEmpty())continue;
            Vec3 clip = clip0.get();
            if(clip.distanceTo(from) > maxDistance)continue;
            return new EntityHitResult(candidate, clip);
        }
        return null;
    }


}
