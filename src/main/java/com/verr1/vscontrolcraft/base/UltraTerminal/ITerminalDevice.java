package com.verr1.vscontrolcraft.base.UltraTerminal;

import java.util.List;

public interface ITerminalDevice {

    List<ExposedFieldWrapper> fields();

    String name();

    default ExposedFieldType exposedFieldType(){return getExposedField().type;}

    default void setExposedField(ExposedFieldType type, double min, double max){}

    default ExposedFieldWrapper getExposedField(){return ExposedFieldWrapper.EMPTY;}

}
