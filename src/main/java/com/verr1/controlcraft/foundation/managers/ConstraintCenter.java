package com.verr1.controlcraft.foundation.managers;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.data.constraint.ConstraintKey;
import com.verr1.controlcraft.foundation.data.constraint.ConstraintWithID;
import com.verr1.controlcraft.foundation.data.constraint.SavedConstraintObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.apigame.joints.VSJoint;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.core.impl.hooks.VSEvents;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ConstraintCenter {
    private static final int lazyTickRate = 3;
    private static int lazyTick = 0;

    private static MinecraftServer server;

    private static final HashMap<ConstraintKey, ConstraintWithID> cache = new HashMap<>();

    public static void onServerStaring(MinecraftServer _server){
        cache.clear();
        server = _server;

        ConstraintSavedData loadedStorage = ConstraintSavedData.load(server);
        List<SavedConstraintObject> constraintList =
                loadedStorage.data
                        .entrySet()
                        .stream()
                        .map(entry -> new SavedConstraintObject(entry.getKey(), entry.getValue()))
                        .toList();

        VSEvents.INSTANCE.getShipLoadEvent().on(((shipLoadEvent, registeredHandler) -> {
            // Execute All Recreating Constrain Tasks Shortly After Any Ship Being Reloaded
            ControlCraftServer
                    .SERVER_DEFERRAL_EXECUTOR
                    .executeLater(() -> constraintList.forEach(ConstraintCenter::createOrReplaceNewConstrain), 4);

            registeredHandler.unregister();
        }));

    }

    public static void onServerStopping(MinecraftServer server){
        ConstraintSavedData storage = ConstraintSavedData.load(server);
        storage.clear();
        cache.forEach((key, data) -> {
            try{
                storage.put(key, data.constrain());
            }catch (Exception e){
                ControlCraft.LOGGER.error("Failed to save constrain", e);
            }
        });
    }

    public static void removeConstraintIfPresent(ConstraintKey key){
        if(cache.containsKey(key)){
            removeConstraint(cache.get(key).ID());
        }
    }

    private static void removeConstraint(int id){
        Optional.ofNullable(ValkyrienSkies.getShipWorld(server))
                .filter(ServerShipWorldCore.class::isInstance)
                .map(ServerShipWorldCore.class::cast)
                .ifPresent(shipWorldCore -> shipWorldCore.removeConstraint(id));
    }

    private static @Nullable Object createNewConstraint(@Nullable VSJoint constrain){
        if(constrain == null)return null;
        return Optional.ofNullable(ValkyrienSkies.getShipWorld(server))
                .filter(ServerShipWorldCore.class::isInstance)
                .map(ServerShipWorldCore.class::cast)
                .map(shipWorldCore -> shipWorldCore.createNewConstraint(constrain))
                .orElse(null);
    }

    public static void createOrReplaceNewConstrain(@NotNull ConstraintKey key, @Nullable VSJoint constrain){
        if(constrain == null)return;
        removeConstraintIfPresent(key);
        Optional.ofNullable(createNewConstraint(constrain))
                .map(Number.class::cast)
                .ifPresent(id -> cache.put(key, new ConstraintWithID(constrain, id.intValue())));
    }

    public static void createOrReplaceNewConstrain(@NotNull SavedConstraintObject obj){
        createOrReplaceNewConstrain(obj.key(), obj.getConstraint());
    }

    public static void destroyAllConstrains(ServerLevel level, BlockPos pos){

    }

    public static VSJoint get(@NotNull ConstraintKey key){
        return cache.containsKey(key) ? cache.get(key).constrain() : null;
    }

    public static boolean isRegistered(ConstraintKey key){
        return cache.containsKey(key);
    }


    public static void lazyTick(){
        if(--lazyTick > 0){
            return;
        }
        lazyTick = lazyTickRate;
    }

    public static void tick(){
        lazyTick();
    }


}
