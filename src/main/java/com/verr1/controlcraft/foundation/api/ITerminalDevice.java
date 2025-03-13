package com.verr1.controlcraft.foundation.api;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.type.ExposedFieldDirection;
import com.verr1.controlcraft.foundation.type.ExposedFieldType;
import com.verr1.controlcraft.utils.MinecraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static java.lang.Math.min;

public interface ITerminalDevice{

    List<ExposedFieldWrapper> fields();

    String name();


    default void setExposedField(ExposedFieldType type, double min, double max, ExposedFieldDirection openTo){
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

    @OnlyIn(Dist.CLIENT)
    default boolean TerminalDeviceToolTip(List<Component> tooltip, boolean isPlayerSneaking) {
        Direction dir = MinecraftUtils.lookingAtFaceDirection();
        if(dir == null)return true;
        tooltip.add(Components.literal("    Face " + dir + " Bounded:"));
        fields().forEach(f -> {
            if(!f.directionOptional.test(dir))return;
            String info = f.type.getComponent().getString();
            tooltip.add(Component.literal(info).withStyle(ChatFormatting.AQUA));
        });

        return true;
    }
}
