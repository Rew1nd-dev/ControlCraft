package com.verr1.controlcraft.foundation.redstone;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.type.descriptive.GroupPolicy;
import com.verr1.controlcraft.foundation.type.descriptive.LerpType;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

import static com.verr1.controlcraft.content.blocks.terminal.TerminalBlockEntity.MAX_CHANNEL_SIZE;

public class RemoteReceiver {



    private final List<RemoteSlotControl> flat = new ArrayList<>();

    private int validSize = 0;

    Map<Integer, GroupPolicy> policyMap = new HashMap<>();
    Map<Integer, LerpType> lerpMap = new HashMap<>();

    public RemoteReceiver() {
        for (int i = 0; i < MAX_CHANNEL_SIZE; i++) {
            flat.add(new RemoteSlotControl());
        }
    }

    public RemoteSlotControl get(int index) {
        return flat.get(index);
    }

    public int validSize() {
        return validSize;
    }

    public void adjustTo(DirectReceiver direct){
        int index = 0;
        for(int i = 0; i < direct.groups.size(); i++){
            DirectSlotGroup group = direct.groups.get(i);
            lerpMap.put(i, group.lerpType);
            policyMap.put(i, group.policy);


            for(int j = 0; j < group.controls.size(); j++){
                DirectSlotControl control = group.controls.get(j);


                if(index >= flat.size()){
                    ControlCraft.LOGGER.error("RemoteReceiver flat size is not enough");
                    return;
                }

                RemoteSlotControl remote = flat.get(index);
                index++;
                remote.groupId = i;
                // follow direct settings
                remote.mutableField = group.field;
                remote.type = control.type();
                remote.suggestedMinMax = control.suggestedMinMax;

                // this should be updated frequently, and adjustTo is called every tick, so it's fine
                // remote.inheritedDirection = control.direction;
            }
        }
        validSize = index;
        for (int i = index; i < flat.size(); i++){
            flat.get(i).groupId = -1;
            flat.get(i).reset();
        }
    }

    public void reset(){
        for (RemoteSlotControl remoteSlotControl : flat) {
            remoteSlotControl.reset();
        }
    }

    public void accept(double signal, int index){
        RemoteSlotControl control = flat.get(index);

        control.store(
                lerpMap
                    .getOrDefault(
                            control.groupId,
                            LerpType.LINEAR)
                    .interpolate
                    .apply(signal / 15)
        );

        int groupId = control.groupId;

        List<Double> all = flat.stream()
                .filter(t -> t.groupId == groupId)
                .filter(t -> t.enabled)
                .map(RemoteSlotControl::latestValue)
                .toList();

        double thisValue = control.latestValue();
        control.mutableField.apply(policyMap.get(groupId).mapping.apply(all, thisValue));
    }


    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putInt("size", flat.size());
        for(int i = 0; i < flat.size(); i++){
            CompoundTag controlTag = flat.get(i).serialize();
            tag.put("control_" + i, controlTag);
        }
        return tag;
    }

    public void deserialize(CompoundTag tag){
        int size = tag.getInt("size");
        for (int i = 0; i < size; i++){
            CompoundTag controlTag = tag.getCompound("control_" + i);
            flat.get(i).deserialize(controlTag);
        }
        for (int i = size; i < flat.size(); i++){
            flat.get(i).groupId = -1;
            flat.get(i).reset();
        }
    }
}
