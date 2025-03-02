package com.verr1.vscontrolcraft.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.verr1.vscontrolcraft.base.DataStructure.ShipHitResult;
import com.verr1.vscontrolcraft.mixinDuck.EntityAccessor;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CCUtils {

    public static ServerContext context;


    public static Map<Integer, ServerComputer> computers = Maps.newConcurrentMap();

    public static ServerComputer getComputerById(int id){
        return computers.getOrDefault(id, null);
    }


    public static Map<String, Double> dumpVec3(Vector3dc vec){
        return Map.of(
                "x", vec.x(),
                "y", vec.y(),
                "z", vec.z()
        );
    }

    public static Map<String, Double> dumpVec3(double x, double y, double z){
        return Map.of(
                "x", x,
                "y", y,
                "z", z
        );
    }

    public static Map<String, Double> dumpVec4(Vector4dc vec){
        return Map.of(
                "x", vec.x(),
                "y", vec.y(),
                "z", vec.z(),
                "w", vec.w()
        );
    }

    public static Map<String, Double> dumpVec4(Quaterniondc vec){
        return Map.of(
                "x", vec.x(),
                "y", vec.y(),
                "z", vec.z(),
                "w", vec.w()
        );
    }

    public static Map<String, Double> dumpVec4(double x, double y, double z, double w){
        return Map.of(
                "x", x,
                "y", y,
                "z", z,
                "w", w
        );
    }

    public static Map<String, Map<String, Double>> dumpAABB(AABBic aabb){
        if(aabb == null)return Map.of();
        return Map.of(
                "min", dumpVec3(aabb.minX(), aabb.minY(), aabb.minZ()),
                "max", dumpVec3(aabb.maxX(), aabb.maxY(), aabb.maxZ())
        );
    }

    public static List<List<Double>> dumpMat3(Matrix3dc mat3){
        List<List<Double>> mat = Lists.newArrayList();
        for (int i = 0; i < 3; i++) {
            Vector3d row = mat3.getRow(i, new Vector3d());
            mat.add(List.of(row.x, row.y, row.z));
        }
        return mat;
    }

    public static Vec3 getEntityVelocity(Entity entity){
        if(entity instanceof EntityAccessor ea){
            return ea.controlCraft$velocityObserver();
        }
        return Vec3.ZERO;
    }

    public static Map<String, Object> parse(EntityHitResult hitResult){
        return Map.of(
                "hit", dumpVec3(Util.Vec3toVector3d(hitResult.getLocation())),
                "type", hitResult.getEntity().getType().toString(),
                "name", hitResult.getEntity().getName().getString(),
                "velocity", dumpVec3(Util.Vec3toVector3d(getEntityVelocity(hitResult.getEntity()))),
                "position", dumpVec3(Util.Vec3toVector3d(hitResult.getEntity().position()))
        );
    }

    public static Map<String, Object> parse(BlockHitResult hitResult){
        return Map.of(
                "hit", Map.of(
                        "x", hitResult.getLocation().x,
                        "y", hitResult.getLocation().y,
                        "z", hitResult.getLocation().z
                ),
                "direction", hitResult.getDirection().getName().toUpperCase()
        );
    }

    public static Map<String, Object> parse(ShipHitResult hitResult){
        return Map.of(
                "hit", dumpVec3(Util.Vec3toVector3d(hitResult.hitLocation())),
                "position", dumpVec3(hitResult.ship().getTransform().getPositionInWorld()),
                "velocity", dumpVec3(hitResult.ship().getVelocity()),
                "AABB", dumpAABB(hitResult.ship().getShipAABB())
        );
    }

    public static Map<String, Object> parse(BlockHitResult hitResult, Level level){
        return Map.of(
                "hit", dumpVec3(Util.Vec3toVector3d(hitResult.getLocation())),
                "direction", hitResult.getDirection().getName().toUpperCase(),
                "onShip", VSGameUtilsKt.isBlockInShipyard(level, hitResult.getBlockPos()),
                "shipHitResult", parseShipHitResult(hitResult.getBlockPos(), level)
        );
    }

    public static Map<String, Object> parseShipHitResult(BlockPos pos, Level level){
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        if(ship == null)return Map.of();
        return Map.of(
                "slug", ship.getSlug() == null ? "null" : ship.getSlug(),
                "position", dumpVec3(ship.getTransform().getPositionInWorld()),
                "velocity", dumpVec3(ship.getVelocity()),
                "AABB", dumpAABB(ship.getShipAABB())
        );
    }

    public static Double[] parseAsArray(ShipHitResult hitResult){

        /*
         *
         * */

        return List.of(
                hitResult.hitLocation().x,
                hitResult.hitLocation().y,
                hitResult.hitLocation().z,

                hitResult.ship().getTransform().getPositionInWorld().x(),
                hitResult.ship().getTransform().getPositionInWorld().y(),
                hitResult.ship().getTransform().getPositionInWorld().z(),

                hitResult.ship().getVelocity().x(),
                hitResult.ship().getVelocity().y(),
                hitResult.ship().getVelocity().z()
        ).toArray(new Double[0]);
    }

}
