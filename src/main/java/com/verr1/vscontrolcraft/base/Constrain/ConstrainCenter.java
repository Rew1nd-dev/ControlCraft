package com.verr1.vscontrolcraft.base.Constrain;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainKey;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainWithID;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.base.ExpirableData;
import com.verr1.vscontrolcraft.utils.VSConstrainSerializeUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.valkyrienskies.core.apigame.constraints.*;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.core.impl.hooks.VSEvents;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.List;

public class ConstrainCenter {
    private static final int lazyTickRate = 3;
    private static int lazyTick = 0;

    // currently the data would not expire
    private static final HashMap<ConstrainKey, ExpirableData<ConstrainWithID>> cache = new HashMap<>();

    private static TrivialConstraintReloadExecutor constraintLoader;

    public static void onServerStaring(MinecraftServer server){
        cache.clear();
        ConstrainSavedData loadedStorage = ConstrainSavedData.load(server);
        List<SavedConstrainObject> constraintList =
                loadedStorage.data
                        .entrySet()
                        .stream()
                        .map(entry -> new SavedConstrainObject(entry.getKey(), entry.getValue()))
                        .toList();


        constraintLoader = new TrivialConstraintReloadExecutor(constraintList);

        VSEvents.INSTANCE.getShipLoadEvent().on(((shipLoadEvent, registeredHandler) -> {
            if(constraintLoader == null)return;
            // Execute All Recreating Constrain Tasks Shortly After Any Ship Being Reloaded
            DeferralExecutor.executeLater(() -> constraintLoader.onShipLoaded(shipLoadEvent.getShip().getId(), server), 4);

            registeredHandler.unregister();
        }));


    }

    public static void onServerStopping(MinecraftServer server){
        ConstrainSavedData storage = ConstrainSavedData.load(server);
        storage.clear();
        cache.forEach((key, data) -> {
            try{
                storage.put(key, data.data().constrain());
            }catch (Exception e){
                ControlCraft.LOGGER.error("Failed to save constrain", e);
            }
        });
    }




    public static void createOrReplaceNewConstrain(ConstrainKey key, VSConstraint constrain, ServerShipWorldCore sswc){
        if(constrain == null)return;
        if(cache.containsKey(key)){
            cache.get(key).forceExpire();
        }
        if(key.ship_1_isGround()){
            constrain = VSConstrainSerializeUtils.convertGroundId(constrain, sswc, key.dimension(), true);
        }
        else if (key.ship_2_isGround()){
            constrain = VSConstrainSerializeUtils.convertGroundId(constrain, sswc, key.dimension(), false);
        }

        Object id = sswc.createNewConstraint(constrain);

        if(id == null)return;

        cachePut(
                key,
                new ExpirableData<>(
                        new ConstrainWithID(
                                constrain,
                                (int) id
                        ),
                        10,
                        (data) -> {
                            sswc.removeConstraint(data.ID());
                            return null;
                        }
                    )
        );
    }



    public static void cachePut( ConstrainKey key, ExpirableData<ConstrainWithID> data){
        cache.put(key, data);
        //if(storage != null)storage.put(key, data.data().constrain());
    }

    public static void cacheRemove(ConstrainKey key){
        cache.remove(key);
        //if(storage != null)storage.remove(key);
    }

    public static void alive(ConstrainKey key){
        if(cache.containsKey(key)){
            cache.get(key).alive();
        }
    }

    public static VSConstraint get(ConstrainKey key){
        return cache.containsKey(key) ? cache.get(key).data().constrain() : null;
    }

    public static boolean has(ConstrainKey key){
        return cache.containsKey(key);
    }


    public static void remove(ConstrainKey key){
        if(cache.containsKey(key)){
            cache.get(key).forceExpire();
            cacheRemove(key);
        }
    }

    public static boolean isRegistered(ConstrainKey key){
        return cache.containsKey(key);
    }


    private static void tickLife(){
        //cache.forEach((key, data) -> data.tick());
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public static void lazyTick(){
        if(--lazyTick > 0){
            return;
        }
        lazyTick = lazyTickRate;
        tickLife();
    }

    public static void tick(){
        lazyTick();
    }

    private static class TrivialConstraintReloadExecutor{
        List<SavedConstrainObject> constraintList;

        public TrivialConstraintReloadExecutor(List<SavedConstrainObject> constraintList){
            this.constraintList = constraintList;
        }

        public void onShipLoaded(Long shipID, MinecraftServer server){
            constraintList.forEach(constraint -> {
                    String dimensionID = constraint.key().dimension();
                    ResourceKey<Level> d = VSGameUtilsKt.getResourceKey(dimensionID);
                    ServerLevel level = server.getLevel(d);
                    if(level == null)return;
                    ServerShipWorldCore sosw = VSGameUtilsKt.getShipObjectWorld(level);
                    ConstrainCenter.createOrReplaceNewConstrain(
                            constraint.key(),
                            constraint.constrain().constraint(),
                            sosw
                    );

                }
            );
        }
    }

    private static class ConstrainReloadExecutor{

        private final ClusteredConstrain constraintToLoad;

        private ConstrainReloadExecutor(List<SavedConstrainObject> constraintList){
            constraintToLoad = ClusteredConstrain.create(constraintList);
        }


        public void onShipLoaded(Long shipID, ServerShipWorldCore sosw){
            Integer groupToLoad = constraintToLoad.getGroupId(shipID);
            if(groupToLoad == null)return;
            constraintToLoad.getGroupConstraints(groupToLoad).forEach(constraint -> ConstrainCenter.createOrReplaceNewConstrain(
                    constraint.key(),
                    constraint.constrain().constraint(),
                    sosw
            ));
            constraintToLoad.deleteGroup(groupToLoad);
        }

        public boolean finished(){
            return constraintToLoad.empty();
        }
    }

}
