package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Hinge.packets.DestroyConstrainPacket;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractMultipleSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(value = Dist.CLIENT)
public class WandDestroyConstrainMode extends WandAbstractMultipleSelectionMode {
    public static final String ID = "destroy_constrain";

    public static WandDestroyConstrainMode instance;

    public static void createInstance(){
        instance = new WandDestroyConstrainMode();
    }

    private WandDestroyConstrainMode(){

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
                .sendToServer(new DestroyConstrainPacket(selection.pos()));
    }


    protected void lazyTick(){
        WandRenderer.textPlayerWhenHoldingWand(tickCallBackInfo());
    }

    @Override
    public String tickCallBackInfo() {
        return "right click to destroy constrain";
    }
}
