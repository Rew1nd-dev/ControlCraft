package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Servo.ServoMotorConstrainAssemblePacket;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class WandServoAssembleMode extends WandAbstractDualSelectionMode {
    public static final String ID = "servo_assemble";

    public static WandServoAssembleMode instance;


    public static void createInstance(){
        instance = new WandServoAssembleMode();
    }

    public WandServoAssembleMode getInstance(){
        return instance;
    }

    private WandServoAssembleMode(){
        super();
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
