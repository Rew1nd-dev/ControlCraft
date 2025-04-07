package com.verr1.controlcraft.content.wand.mode;

import com.verr1.controlcraft.content.wand.mode.base.WandAbstractMultipleSelectionMode;
import com.verr1.controlcraft.foundation.api.IWandMode;
import com.verr1.controlcraft.foundation.data.WandSelection;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.type.WandModesType;
import com.verr1.controlcraft.registry.ControlCraftPackets;

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
        var p = new GenericServerPacket.builder(RegisteredPacketType.DESTROY_ALL_CONSTRAIN)
                .withLong(selection.pos().asLong())
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
    }



    @Override
    public String tickCallBackInfo() {
        return WandModesType.DESTROY.tickCallBackInfo(state).getString();
    }  //"right click to destroy constrain"

}
