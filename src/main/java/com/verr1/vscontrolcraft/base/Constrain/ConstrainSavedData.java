package com.verr1.vscontrolcraft.base.Constrain;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainKey;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;

import java.util.HashMap;

import static com.verr1.vscontrolcraft.ControlCraft.MODID;

public class ConstrainSavedData extends SavedData {
    private static final String DATA_NAME = MODID + "_Constrains";

    public final HashMap<ConstrainKey, ConstrainSerializable> data = new HashMap<>();

    public static ConstrainSavedData create(){
        return new ConstrainSavedData();
    }

    private static ConstrainSavedData load(@NotNull CompoundTag tag) {
        ConstrainSavedData savedData = new ConstrainSavedData();
        CompoundTag savedTag = tag.getCompound("data");

        try {
            int size = tag.getInt("size");
            for(int i = 0; i < size; i++){
                var savedConstrain = SavedConstrainObject.deserialize(savedTag.getCompound(String.valueOf(i)));
                savedData.data.put(savedConstrain.key(), savedConstrain.constrain());
            }
        } catch (Exception e){
            ControlCraft.LOGGER.error("Failed to load ConstrainSavedData", e);
        }

        return savedData;
    }


    public void clear(){
        data.clear();
        setDirty();
    }


    public static ConstrainSavedData load(MinecraftServer server){
        return server.overworld().getDataStorage().computeIfAbsent(ConstrainSavedData::load, ConstrainSavedData::create, ConstrainSavedData.DATA_NAME);
    }


    public void put(ConstrainKey key, VSConstraint constrain){
        data.put(key, new ConstrainSerializable(constrain));
        setDirty();
    }

    public void remove(ConstrainKey key){
        data.remove(key);
        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag savedTag = new CompoundTag();
        int saveCounter = 0;
        for(var entry : data.entrySet()){
            savedTag.put(String.valueOf(saveCounter), new SavedConstrainObject(entry.getKey(), entry.getValue()).serialize());
            saveCounter++;
        }

        tag.put("data", savedTag);
        tag.putInt("size", data.size());

        return tag;
    }


}
