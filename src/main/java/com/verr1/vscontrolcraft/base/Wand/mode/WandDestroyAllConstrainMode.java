package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Hinge.packets.DestroyAllConstrainPacket;
import com.verr1.vscontrolcraft.base.Hinge.packets.DestroyConstrainPacket;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractMultipleSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandModesType;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.registry.AllPackets;

public class WandDestroyAllConstrainMode extends WandAbstractMultipleSelectionMode {

    public static WandDestroyAllConstrainMode instance;

    public static void createInstance(){
        instance = new WandDestroyAllConstrainMode();
    }

    @Override
    public IWandMode getInstance() {
        return instance;
    }

    @Override
    public String getID() {
        return "destroy_all";
    }

    @Override
    public void onSelection(WandSelection selection) {
        AllPackets
                .getChannel()
                .sendToServer(new DestroyAllConstrainPacket(selection.pos()));
    }


    protected void lazyTick(){
        WandRenderer.textPlayerWhenHoldingWand(tickCallBackInfo());
    }

    @Override
    public String tickCallBackInfo() {
        return WandModesType.DESTROY.tickCallBackInfo(state).getString();
    }  //"right click to destroy constrain"

}
