package com.verr1.controlcraft.foundation.vsapi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.ClientShipWorld;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.core.apigame.world.ClientShipWorldCore;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.core.apigame.world.ShipWorldCore;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class ValkyrienSkies {

    public static ServerShipWorld getShipWorld(MinecraftServer server){
        return VSGameUtilsKt.getShipObjectWorld(server);
    }

    public static ServerShipWorldCore getShipWorld(ServerLevel level){
        return VSGameUtilsKt.getShipObjectWorld(level);
    }

    public static ShipWorldCore getShipWorld(Level level){
        return VSGameUtilsKt.getShipObjectWorld(level);
    }

    public static ClientShipWorldCore getShipWorld(ClientLevel level){
        return VSGameUtilsKt.getShipObjectWorld(level);
    }

    public static ClientShipWorldCore getShipWorld(Minecraft client){
        return VSGameUtilsKt.getShipObjectWorld(client);
    }

    public static Ship getShipManagingBlock(@Nullable Level level, BlockPos pos){
        return VSGameUtilsKt.getShipManagingPos(level, pos);
    }

    public static Vector3d set(Vector3d dest, Vec3i source){
        return VectorConversionsMCKt.set(dest, source);
    }

    public static Vector3d set(Vector3d dest, Vec3 source){
        return VectorConversionsMCKt.set(dest, source);
    }

    public static Vector3i set(Vector3i dest, Vec3i source){
        return VectorConversionsMCKt.set(dest, source);
    }

    public static String getDimensionId(Level level){
        return VSGameUtilsKt.getDimensionId(level);
    }

    public static Vector3d toJOML(Vec3 source){
        return VectorConversionsMCKt.toJOML(source);
    }

    public static Vec3 toMinecraft(Vector3dc source){
        return VectorConversionsMCKt.toMinecraft(source);
    }

    public static AABBd toJOML(AABB source){
        return VectorConversionsMCKt.toJOML(source);
    }

    public static AABB toMinecraft(AABBdc source){
        return VectorConversionsMCKt.toMinecraft(source);
    }


}
