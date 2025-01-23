package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointConstrainAssemblePacket;
import com.verr1.vscontrolcraft.registry.AllPackets;

public class WandJointAssembleMode extends WandAbstractQuadSelectionMode{
    public static final String ID = "joint_assemble";
    public static WandJointAssembleMode instance;


    @Override
    public IWandMode getInstance() {
        return instance;
    }

    public static void createInstance(){
        instance = new WandJointAssembleMode();
    }

    @Override
    public void onSelection(WandSelection selection) {
        if(state == State.TO_SELECT_Y){
            if(selection.pos().equals(x.pos()))return;
        }
        if(state == State.TO_SELECT_Z){
            if(!selection.pos().equals(x.pos()))return;
            if(selection.face() == x.face() || selection.face() == x.face().getOpposite())return;
        }
        if(state == State.TO_SELECT_W){
            //if(!selection.pos().equals(y.pos()))return;
            if(selection.face() == y.face() || selection.face() == y.face().getOpposite())return;
        }
        super.onSelection(selection);
    }



    @Override
    protected void sendPacket(WandSelection x, WandSelection y, WandSelection z, WandSelection w) {
        AllPackets
                .getChannel()
                .sendToServer(
                        new RevoluteJointConstrainAssemblePacket(
                        y.pos(),
                        x.pos(),
                        y.face(),
                        x.face(),
                        z.face(),
                        w.face()
                )
            );
    }

    @Override
    public String getID() {
        return ID;
    }
}
