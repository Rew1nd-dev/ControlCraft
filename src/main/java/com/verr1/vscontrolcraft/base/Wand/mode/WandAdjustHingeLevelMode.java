package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Hinge.HingeAdjustLevelPacket;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.registry.AllPackets;

public class WandAdjustHingeLevelMode implements IWandMode {
    public static final String ID = "adjust_hinge_level";

    public static WandAdjustHingeLevelMode instance;

    public static void createInstance(){
        instance = new WandAdjustHingeLevelMode();
    }

    private WandAdjustHingeLevelMode(){

    }

    @Override
    public IWandMode getInstance() {
        return instance;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void onSelection(WandSelection selection) {
        AllPackets
                .getChannel()
                .sendToServer(new HingeAdjustLevelPacket(selection.pos()));
    }
}
