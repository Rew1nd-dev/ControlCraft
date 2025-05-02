package com.verr1.controlcraft.content.gui.wand.mode;

import com.verr1.controlcraft.content.gui.wand.mode.base.WandAbstractMultipleSelectionMode;
import com.verr1.controlcraft.foundation.api.IWandMode;
import com.verr1.controlcraft.foundation.data.WandSelection;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.type.WandModesType;
import com.verr1.controlcraft.registry.ControlCraftPackets;
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
        var p = new GenericServerPacket.builder(RegisteredPacketType.DESTROY_CONSTRAIN)
                    .withLong(selection.pos().asLong())
                    .build();
        ControlCraftPackets.getChannel().sendToServer(p);
    }


    protected void lazyTick(){

    }

    @Override
    public String tickCallBackInfo() {
        return WandModesType.DESTROY.tickCallBackInfo(state).getString();
    }  //"right click to destroy constrain"
}
