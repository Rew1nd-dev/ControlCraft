package com.verr1.controlcraft.foundation.api;

import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import net.minecraftforge.network.NetworkEvent;

public interface IPacketHandler {
    default void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet){};

    default void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet){};

}
