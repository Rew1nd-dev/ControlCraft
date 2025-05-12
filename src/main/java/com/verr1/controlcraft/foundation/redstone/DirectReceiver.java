package com.verr1.controlcraft.foundation.redstone;

import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.NumericField;
import com.verr1.controlcraft.foundation.type.descriptive.MiscDescription;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.utils.MinecraftUtils;
import kotlin.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public class DirectReceiver {


    final List<DirectSlotGroup> groups = new ArrayList<>();
    final List<DirectSlotGroup> view = Collections.unmodifiableList(groups);


    public DirectReceiver register(NumericField field, InitContext... initContexts){
        SlotType main = initContexts[0].type.toMain();
        List<DirectSlotControl> controls = new ArrayList<>();
        Arrays.stream(initContexts).forEach(
                t -> controls.add(new DirectSlotControl(t.type()).suggest(t.suggest(), t.type().isBoolean()))
        );
        groups.add(new DirectSlotGroup(field, main, controls));
        return this;
    }

    public DirectReceiver register(NumericField field, InitContext initContexts, int duplicate){
        SlotType main = initContexts.type;
        List<DirectSlotControl> controls = new ArrayList<>();
        for (int i = 0; i < duplicate; i++) {
            controls.add(new DirectSlotControl(initContexts.type()).suggest(initContexts.suggest(), initContexts.type().isBoolean()));
        }
        groups.add(new DirectSlotGroup(field, main, controls));
        return this;
    }

    public List<DirectSlotGroup> view() {
        return view;
    }

    public void accept(int signal, Direction direction){
        List<Pair<DirectSlotGroup, DirectSlotControl>> toUpdate = new ArrayList<>(groups.size());
        groups.forEach(
            g -> g.controls
                    .stream()
                    .filter(c -> c.direction.test(direction))
                    .forEach(c -> {
                        c.store((double)signal / 15);
                        toUpdate.add(new Pair<>(g, c));
                    })
        );

        toUpdate.forEach(
            p -> update(p.getFirst(), p.getSecond())
        );
    }

    private void update(DirectSlotGroup group, DirectSlotControl control){
        List<Double> all = group.controls.stream().map(DirectSlotControl::latestValue).toList();
        double thisValue = control.latestValue();
        group.field.apply(group.policy.mapping.apply(all, thisValue));
    }

    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putInt("size", groups.size());
        for(int i = 0; i < groups.size(); i++){
            CompoundTag groupTag = groups.get(i).serialize();
            tag.put("group_" + i, groupTag);
        }
        return tag;
    }

    public void deserialize(CompoundTag tag){
        for (int i = 0; i < groups.size(); i++){
            CompoundTag groupTag = tag.getCompound("group_" + i);
            groups.get(i).deserialize(groupTag);
        }
    }

    public void deserializeClientView(CompoundTag tag){
        int size = tag.getInt("size");
        groups.clear();
        for (int i = 0; i < size; i++){
            CompoundTag groupTag = tag.getCompound("group_" + i);
            groups.add(new DirectSlotGroup(NumericField.EMPTY, SlotType.NONE, new ArrayList<>()));
            groups.get(i).deserializeClientView(groupTag);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public boolean makeToolTip(List<Component> tooltip, boolean $){
        Direction dir = MinecraftUtils.lookingAtFaceDirection();
        if(dir == null)return true;
        tooltip.add(Component.literal("    ")
                .append(MiscDescription.FACE_BOUND.specificFlat()).withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" [" + dir.getName() + "]:").withStyle(ChatFormatting.LIGHT_PURPLE))
        );
        groups.forEach(
                g -> g.controls
                        .stream()
                        .filter(c -> c.direction.test(dir))
                        .forEach(
                                c -> {
                                    int index = g.controls.indexOf(c);
                                    tooltip.add(
                                            c.type().asComponent().copy().withStyle(s -> s.withColor(ChatFormatting.AQUA))
                                                    .append(
                                            Component.literal(" [" + index + "]")
                                                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                                                    .withStyle(ChatFormatting.ITALIC)

                                    ));
                                }
                        )
        );


        return true;
    }

    public record InitContext(SlotType type, Couple<Double> suggest){}

}
