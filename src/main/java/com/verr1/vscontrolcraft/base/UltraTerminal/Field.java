package com.verr1.vscontrolcraft.base.UltraTerminal;

public interface Field<T> {


    T value();

    void apply(T value);

    String name();

    WidgetType widgetType();

}
