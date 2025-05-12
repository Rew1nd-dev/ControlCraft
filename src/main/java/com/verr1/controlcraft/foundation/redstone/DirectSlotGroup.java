package com.verr1.controlcraft.foundation.redstone;

import com.verr1.controlcraft.foundation.data.NumericField;
import com.verr1.controlcraft.foundation.type.descriptive.GroupPolicy;
import com.verr1.controlcraft.foundation.type.descriptive.LerpType;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectSlotGroup {



    final NumericField field;

    int validSize;

    private SlotType type;

    final List<DirectSlotControl> controls = new ArrayList<>();

    final List<DirectSlotControl> view = Collections.unmodifiableList(controls);




    LerpType lerpType = LerpType.LINEAR;
    GroupPolicy policy = GroupPolicy.EXCLUSIVE;

    public GroupPolicy policy() {
        return policy;
    }

    public LerpType lerpType() {
        return lerpType;
    }

    public void setPolicy(GroupPolicy policy) {
        this.policy = policy;
    }

    public void setLerpType(LerpType lerpType) {
        this.lerpType = lerpType;
    }

    public List<DirectSlotControl> view() {
        return view;
    }
    public NumericField field() {
        return field;
    }

    public SlotType type() {
        return type;
    }

    public DirectSlotGroup(
            NumericField field,
            SlotType mainType,
            List<DirectSlotControl> controls
    ) {
        this.field = field;
        this.type = mainType;
        this.controls.addAll(controls);
        this.validSize = controls.size();
    }

    public DirectSlotGroup(
            NumericField field,
            SlotType mainType,
            List<DirectSlotControl> controls,
            int validSize
    ) {
        if(validSize > controls.size()){
            throw new IllegalArgumentException("validSize cannot be greater than controls size");
        }
        this.field = field;
        this.type = mainType;
        this.controls.addAll(controls);
        this.validSize = validSize;
    }


    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        for(int i = 0; i < controls.size(); i++){
            CompoundTag controlTag = controls.get(i).serialize();
            tag.put("control_" + i, controlTag);
        }
        tag.putInt("size", controls.size());
        tag.putInt("validSize", validSize);
        tag.put("policy", SerializeUtils.ofEnum(GroupPolicy.class).serialize(policy));
        tag.put("type", SerializeUtils.ofEnum(SlotType.class).serialize(type));
        tag.put("lerp", SerializeUtils.ofEnum(LerpType.class).serialize(lerpType));
        return tag;
    }


    public void deserialize(CompoundTag tag){
        for (int i = 0; i < controls.size(); i++){
            CompoundTag controlTag = tag.getCompound("control_" + i);
            controls.get(i).deserialize(controlTag);
        }
        policy = SerializeUtils.ofEnum(GroupPolicy.class).deserialize(tag.getCompound("policy"));
        type = SerializeUtils.ofEnum(SlotType.class).deserialize(tag.getCompound("type"));
        lerpType = SerializeUtils.ofEnum(LerpType.class).deserialize(tag.getCompound("lerp"));
        validSize = SerializeUtils.INT.deserialize(tag.getCompound("validSize"));
    }

    public void deserializeClientView(CompoundTag tag){
        controls.clear();
        int size = tag.getInt("size");
        validSize = SerializeUtils.INT.deserialize(tag.getCompound("validSize"));
        policy = SerializeUtils.ofEnum(GroupPolicy.class).deserialize(tag.getCompound("policy"));
        type = SerializeUtils.ofEnum(SlotType.class).deserialize(tag.getCompound("type"));
        lerpType = SerializeUtils.ofEnum(LerpType.class).deserialize(tag.getCompound("lerp"));
        for (int i = 0; i < size; i++){
            CompoundTag controlTag = tag.getCompound("control_" + i);
            controls.add(new DirectSlotControl(type));
            controls.get(i).deserialize(controlTag);
        }
    }

}
