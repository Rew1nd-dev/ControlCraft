package com.verr1.vscontrolcraft.base;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Function;

public class SyncAnimationPacket<T extends BlockEntity, P extends IPacketHandle<T>> extends SimplePacketBase {
    private P packetHandler;
    private BlockPos pos;
    private Class<T> blockEntityClass;


    /*
    public SyncAnimationPacket(T be, P handle, Class<T> blockEntityClass) {
        pos = be.getBlockPos();
        PacketHandler = handle;
        this.blockEntityClass = blockEntityClass;
    }
    **/
    public SyncAnimationPacket(T be, P handle, Class<T> blockEntityClass) {
        pos = be.getBlockPos();
        packetHandler = handle;
        this.blockEntityClass = blockEntityClass;
    }

    public SyncAnimationPacket(FriendlyByteBuf buffer, P handler, Class<T> blockEntityClass) {
        this.packetHandler = handler;
        this.blockEntityClass = blockEntityClass;
        this.packetHandler.readBuffer(buffer);
        this.pos = buffer.readBlockPos();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        packetHandler.writeBuffer(buffer);
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().player.level().getExistingBlockEntity(pos);
            if (blockEntityClass.isInstance(blockEntity)) {
                T castedBlockEntity = blockEntityClass.cast(blockEntity);
                packetHandler.handle(castedBlockEntity);
            }
        });
        return true;
    }

    public static <T extends BlockEntity, P extends IPacketHandle<T>>
    Function<FriendlyByteBuf, SyncAnimationPacket<T, P>>
    createDecoder(
            Class<T> blockEntityClass, P handlerInstance
    ) {
        return buffer -> new SyncAnimationPacket<>(buffer, handlerInstance, blockEntityClass);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity, P extends IPacketHandle<T>>
    Class<SyncAnimationPacket<T, P>>
    getPacketClass()
    {
        return (Class<SyncAnimationPacket<T, P>>) (Class<?>) SyncAnimationPacket.class;
    }
}
