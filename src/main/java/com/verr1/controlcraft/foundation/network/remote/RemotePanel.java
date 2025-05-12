package com.verr1.controlcraft.foundation.network.remote;

import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.packets.specific.RemotePacket;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class RemotePanel {


    private final Map<NetworkKey, RemotePort<?>> responses = new HashMap<>();


    public void request(Object input, BlockPos boundPos, NetworkKey key) {
        if(!responses.containsKey(key))return;
        CompoundTag tag = responses.get(key).serialize(input);
        CompoundTag keyTag = key.serialize();
        CompoundTag total = new CompoundTag();
        total.put("key", keyTag);
        total.put("data", tag);
        ControlCraftPackets.getChannel().sendToServer(new RemotePacket(boundPos, total));
    }

    public void receive(CompoundTag tag) {
        NetworkKey key = NetworkKey.deserialize(tag.getCompound("key"));
        RemotePort<?> port = responses.get(key);
        if (port == null)return;
        Object input = port.deserialize(tag);
        if (input == null)return;
        port.accept(input);
    }


    public <T> void register(NetworkKey key, RemotePort<T> port) {
        responses.put(key, port);
    }

    public <T> void registerUnit(NetworkKey key, Runnable task) {
        register(
                key,
                RemotePort.of(
                        Double.class,
                        $ -> task.run(),
                        SerializeUtils.DOUBLE
                )
        );
    }

}
