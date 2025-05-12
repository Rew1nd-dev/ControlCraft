package com.verr1.controlcraft.foundation.api.delegate;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.type.descriptive.SlotDirection;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.utils.MinecraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static java.lang.Math.min;

public interface ITerminalDevice{
    public static NetworkKey FIELD__ = NetworkKey.create("field");

    List<ExposedFieldWrapper> fields();

    String name();


    default void setExposedField(SlotType type, double min, double max, SlotDirection openTo){
        fields().stream()
                .filter(f -> f.type == type)
                .findFirst()
                .ifPresent(f -> {
                    f.min_max = Couple.create(min, max);
                    f.directionOptional = openTo;
                });
    }


    default void accept(int signal, Direction direction){
        fields().stream()
                .filter(f -> f.directionOptional.test(direction))
                .forEach(f -> f.apply((double)signal / 15));
    }

    default void reset(){
        fields().forEach(ExposedFieldWrapper::reset);
    }

    default CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name());
        tag.putInt("fields", fields().size());
        for (int i = 0; i < fields().size(); i++) {
            tag.put("field" + i, fields().get(i).serialize());
        }
        return tag;
    }

    default void deserialize(CompoundTag tag){
        if(!tag.getString("name").equals(name()))return;
        deserializeUnchecked(tag);
    }

    default void deserializeUnchecked(CompoundTag tag){
        // if(!tag.getString("name").equals(name()))return;
        int size = tag.getInt("fields");
        for (int i = 0; i < min(size, fields().size()); i++) {
            fields().get(i).deserialize(tag.getCompound("field" + i));
        }
    }


    @OnlyIn(Dist.CLIENT)
    default boolean TerminalDeviceToolTip(List<Component> tooltip, boolean isPlayerSneaking) {
        Direction dir = MinecraftUtils.lookingAtFaceDirection();
        if(dir == null)return true;
        tooltip.add(Components.literal("    Face " + dir + " Bounded:"));
        fields().forEach(f -> {
            if(!f.directionOptional.test(dir))return;
            String info = f.type.asComponent().getString();
            tooltip.add(Component.literal(info).withStyle(ChatFormatting.AQUA));
        });

        return true;
    }
}
