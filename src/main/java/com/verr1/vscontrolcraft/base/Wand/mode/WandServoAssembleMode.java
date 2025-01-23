package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorConstrainAssemblePacket;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.registry.AllPackets;

public class WandServoAssembleMode extends WandAbstractDualSelectionMode {
    public static final String ID = "servo_assemble";

    public static WandServoAssembleMode instance;


    public static void createInstance(){
        instance = new WandServoAssembleMode();
    }

    public WandServoAssembleMode getInstance(){
        return instance;
    }


    @Override
    public String getID() {
        return ID;
    }

    @Override
    protected void sendPacket(WandSelection x, WandSelection y) {
        AllPackets
                .getChannel()
                .sendToServer(
                        new ServoMotorConstrainAssemblePacket(
                                x.pos(),
                                y.pos(),
                                x.face(),
                                y.face()
                        )
                );
    }

}
