package com.verr1.vscontrolcraft.blocks.camera;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class ServerCameraManager {
    private static final HashMap<UUID, LevelPos> player2Camera = new HashMap<>();
    private static final HashMap<LevelPos, UUID> camera2Player = new HashMap<>();

    public static void registerUser(LevelPos cameraPos, ServerPlayer player){
        player2Camera.put(player.getUUID(), cameraPos);
        camera2Player.put(cameraPos, player.getUUID());
    }

    public static void remove(LevelPos pos){
        UUID player = camera2Player.get(pos);
        player2Camera.remove(player);
        camera2Player.remove(pos);
    }

    public static boolean isRegistered(LevelPos pos){
        return camera2Player.containsKey(pos);
    }

    public static boolean isRegistered(UUID player){
        return player2Camera.containsKey(player);
    }

    public static void remove(UUID player){
        LevelPos pos = player2Camera.get(player);
        player2Camera.remove(player);
        camera2Player.remove(pos);
    }

    public static ServerPlayer getUser(LevelPos pos){
        return pos.level().getServer().getPlayerList().getPlayer(camera2Player.get(pos));
    }

    public static LevelPos getCamera(ServerPlayer player){
        return player2Camera.get(player.getUUID());
    }

    public static @Nullable ChunkPos getCameraChunk(ServerPlayer player){
        if(isRegistered(player.getUUID())) return new ChunkPos(player2Camera.get(player.getUUID()).pos());
        return null;
    }

    public static boolean hasNearByCamera(Vec3 position){
        for(LevelPos pos : camera2Player.keySet()){
            if(pos.pos().getCenter().distanceTo(position) < 10 * 16){
                return true;
            }
        }
        return false;
    }



}
