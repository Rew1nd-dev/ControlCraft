package com.verr1.controlcraft.foundation.network.handler;

import com.verr1.controlcraft.foundation.network.packets.GenericClientPacket;
import net.minecraftforge.network.NetworkEvent;

public class ClientGenericPacketHandler {

    public static void dispatchPacket(GenericClientPacket packet, NetworkEvent.Context context) {
        switch (packet.getType()){

            default: break;
        }
    }

}
