package com.verr1.vscontrolcraft.base.UltraTerminal;

import java.util.List;

public interface ITerminalDevice {

    List<NumericField> fields();

    String name();
}
