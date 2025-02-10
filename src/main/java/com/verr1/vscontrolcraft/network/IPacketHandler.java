package com.verr1.vscontrolcraft.network;

import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import net.minecraftforge.network.NetworkEvent;

public interface IPacketHandler {

    default void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet){};

    default void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet){};


}
