package com.verr1.controlcraft.foundation.managers;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.data.constraint.ConstraintKey;
import com.verr1.controlcraft.foundation.data.constraint.ConstraintSerializable;
import com.verr1.controlcraft.foundation.data.constraint.SavedConstraintObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;


import java.util.HashMap;

import static com.verr1.controlcraft.ControlCraft.MODID;

public class ConstraintSavedData extends SavedData {

    private static final String DATA_NAME = MODID + "_constrains";

    public final HashMap<ConstraintKey, ConstraintSerializable> data = new HashMap<>();

    public static ConstraintSavedData create(){
        return new ConstraintSavedData();
    }

    private static ConstraintSavedData load(@NotNull CompoundTag tag) {
        ConstraintSavedData savedData = new ConstraintSavedData();
        CompoundTag savedTag = tag.getCompound("data");

        int size = tag.getInt("size");
        ControlCraft.LOGGER.info("Loading {} constrains", size);
        for(int i = 0; i < size; i++){
            try {
                var savedConstrain = SavedConstraintObject.deserialize(savedTag.getCompound(String.valueOf(i)));
                savedData.data.put(savedConstrain.key(), savedConstrain.constrain());
            } catch (Exception e){
                ControlCraft.LOGGER.error("Failed to load ConstraintSavedData", e);
            }

        }




        return savedData;
    }


    public void clear(){
        data.clear();
        setDirty();
    }


    public static ConstraintSavedData load(MinecraftServer server){
        return server.overworld().getDataStorage().computeIfAbsent(ConstraintSavedData::load, ConstraintSavedData::create, ConstraintSavedData.DATA_NAME);
    }


    public void put(ConstraintKey key, VSConstraint constrain){
        data.put(key, new ConstraintSerializable(constrain));
        setDirty();
    }

    public void remove(ConstraintKey key){
        data.remove(key);
        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag savedTag = new CompoundTag();
        int saveCounter = 0;
        for(var entry : data.entrySet()){
            try{
                savedTag.put(String.valueOf(saveCounter), new SavedConstraintObject(entry.getKey(), entry.getValue()).serialize());
                saveCounter++;
            }catch (Exception e){
                ControlCraft.LOGGER.error("Failed to save ConstraintSavedData", e);
            }
        }

        tag.put("data", savedTag);
        tag.putInt("size", data.size());
        ControlCraft.LOGGER.info("Saved {} constrains", data.size());
        return tag;
    }

}
