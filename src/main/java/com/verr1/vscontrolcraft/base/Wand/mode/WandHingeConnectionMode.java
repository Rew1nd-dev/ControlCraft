package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Hinge.HingeBruteConnectPacket;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class WandHingeConnectionMode extends WandAbstractDualSelectionMode {
    public static final String ID = "hinge_connection";

    public static WandHingeConnectionMode instance;


    public static void createInstance(){
        instance = new WandHingeConnectionMode();
    }

    public WandHingeConnectionMode getInstance(){
        return instance;
    }

    private WandHingeConnectionMode(){
        super();
    }

    @Override
    public String getID() {
        return ID;
    }


    @Override
    protected void sendPacket(WandSelection x, WandSelection y) {
        AllPackets.getChannel().sendToServer(new HingeBruteConnectPacket(y.pos(), x.pos())); // x.bruteConnect(y)
    }


}
