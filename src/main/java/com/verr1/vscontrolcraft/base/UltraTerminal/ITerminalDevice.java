package com.verr1.vscontrolcraft.base.UltraTerminal;

import net.minecraft.core.Direction;

import java.util.List;

public interface ITerminalDevice {

    List<ExposedFieldWrapper> fields();

    String name();

    default ExposedFieldType exposedFieldType(){return getExposedField().type;}

    default void setExposedField(ExposedFieldType type, double min, double max, ExposedFieldDirection openTo){}

    default ExposedFieldWrapper getExposedField(){return ExposedFieldWrapper.EMPTY;}

    default void accept(int signal, Direction direction){
        fields()
                .stream()
                .filter(f -> f.directionOptional.test(direction))
                .forEach(f -> f.apply(signal));
    }

}
