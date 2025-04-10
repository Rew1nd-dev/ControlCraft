package com.verr1.controlcraft.foundation.managers;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.data.constraint.ConstraintKey;
import com.verr1.controlcraft.foundation.data.constraint.ConstraintWithID;
import com.verr1.controlcraft.foundation.data.constraint.SavedConstraintObject;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.mixin.accessor.ShipObjectServerWorldAccessor;
import com.verr1.controlcraft.utils.VSGetterUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.impl.hooks.VSEvents;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.ArrayList;
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

        VSEvents.ShipLoadEvent.Companion.on(((shipLoadEvent, registeredHandler) -> {
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
            ControlCraft.LOGGER.info("Removing Constraint: " + key);
            removeConstraint(cache.get(key).ID());
        }
    }

    private static void removeConstraint(int id){
        Optional.ofNullable(ValkyrienSkies.getShipWorld(server))
                .filter(ServerShipWorldCore.class::isInstance)
                .map(ServerShipWorldCore.class::cast)
                .ifPresent(shipWorldCore -> shipWorldCore.removeConstraint(id));
    }

    public static void destroyAllConstrains(ServerLevel level, BlockPos pos){
        try{
            ServerShipWorldCore sswc = VSGameUtilsKt.getShipObjectWorld(level);
            ShipObjectServerWorldAccessor accessor = ((ShipObjectServerWorldAccessor) sswc);
            var constraints = accessor.controlCraft$getShipIdToConstraints();
            long id = VSGetterUtils.getShip(level, pos).map(Ship::getId).orElse(-1L);
            if(id == -1)return;
            new ArrayList<>(constraints.get(id)).forEach(sswc::removeConstraint);

        }catch (Exception ignored){
            ControlCraft.LOGGER.error("Failed to destroy all constraints", ignored);
        }
    }

    private static @Nullable Object createNewConstraint(@Nullable VSConstraint constraint){
        if(constraint == null)return null;
        ControlCraft.LOGGER.info("Creating New Constraint: " + constraint.getConstraintType());
        return Optional.ofNullable(ValkyrienSkies.getShipWorld(server))
                .filter(ServerShipWorldCore.class::isInstance)
                .map(ServerShipWorldCore.class::cast)
                .map(shipWorldCore -> shipWorldCore.createNewConstraint(constraint))
                .orElse(null);
    }

    private static boolean updateConstraint(int id, VSConstraint constraint){
        if(constraint == null)return false;
        ControlCraft.LOGGER.info("Updating Constraint: " + constraint.getConstraintType());
        return Optional
                .ofNullable(ValkyrienSkies.getShipWorld(server))
                .filter(ShipObjectServerWorld.class::isInstance)
                .map(ShipObjectServerWorld.class::cast)
                .map(shipWorldCore -> shipWorldCore.updateConstraint(id, constraint)).orElse(false);
    }

    public static void createOrReplaceNewConstrain(@NotNull ConstraintKey key, @Nullable VSConstraint constraint){
        if(constraint == null)return;
        removeConstraintIfPresent(key);
        Optional.ofNullable(createNewConstraint(constraint))
                .map(Number.class::cast)
                .ifPresent(id -> {
                    cache.put(key, new ConstraintWithID(constraint, id.intValue()));
                    ControlCraft.LOGGER.info("Created Constraint: " + constraint.getConstraintType() + " ID: " + id);
                });
    }

    public static void updateOrCreateConstraint(ConstraintKey key, VSConstraint constraint){
        if(!cache.containsKey(key)){
            createOrReplaceNewConstrain(key, constraint);
        }
        else{
            ConstraintWithID data = cache.get(key);
            if(!updateConstraint(data.ID(), constraint)){
                ControlCraft.LOGGER.info("Failed to Update Constraint: " + constraint.getConstraintType());
                removeConstraint(data.ID());
                createOrReplaceNewConstrain(key, constraint);
            }else{
                ControlCraft.LOGGER.info("Updated Constraint: " + constraint.getConstraintType());
                cache.put(key, new ConstraintWithID(constraint, data.ID()));
            }
        }
    }



    public static void createOrReplaceNewConstrain(@NotNull SavedConstraintObject obj){
        createOrReplaceNewConstrain(obj.key(), obj.getConstraint());
    }

    public static VSConstraint get(@NotNull ConstraintKey key){
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
