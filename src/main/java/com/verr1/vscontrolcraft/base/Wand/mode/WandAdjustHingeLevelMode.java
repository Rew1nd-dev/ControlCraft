package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Hinge.packets.HingeAdjustLevelPacket;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractMultipleSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class WandAdjustHingeLevelMode extends WandAbstractMultipleSelectionMode {
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


    protected void lazyTick(){
        WandRenderer.textPlayerWhenHoldingWand(tickCallBackInfo());
    }

    @Override
    public String tickCallBackInfo() {
        return "right click hinge to adjust level";
    }
}
