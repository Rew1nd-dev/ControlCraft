package com.verr1.controlcraft.registry;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.network.packets.GenericClientPacket;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum ControlCraftPackets {
    BLOCK_BOUND_CLIENT(BlockBoundClientPacket.class, BlockBoundClientPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    GENERIC_CLIENT(GenericClientPacket.class, GenericClientPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    EXPOSED_FIELD_OPEN_SCREEN(ExposedFieldOpenScreenPacket.class, ExposedFieldOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    EXPOSED_FIELD_SYNC_CLIENT(ExposedFieldSyncClientPacket.class, ExposedFieldSyncClientPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    SYNC_BLOCKENTITY_CLIENT(SyncBlockEntityClientPacket.class, SyncBlockEntityClientPacket::new, NetworkDirection.PLAY_TO_CLIENT),

    GENERIC_SERVER(GenericServerPacket.class, GenericServerPacket::new, NetworkDirection.PLAY_TO_SERVER),
    BLOCK_BOUND_SERVER(BlockBoundServerPacket.class, BlockBoundServerPacket::new, NetworkDirection.PLAY_TO_SERVER),
    SETTING_EXPOSED_FIELD(ExposedFieldSettingsPacket.class, ExposedFieldSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    TERMINAL_SETTINGS(TerminalSettingsPacket.class, TerminalSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    REQUEST_SYNC(LazyRequestBlockEntitySyncPacket.class, LazyRequestBlockEntitySyncPacket::new, NetworkDirection.PLAY_TO_SERVER),
    SYNC_BLOCKENTITY_SERVER(SyncBlockEntityServerPacket.class, SyncBlockEntityServerPacket::new, NetworkDirection.PLAY_TO_SERVER),
    ;



    public static final String NETWORK_VERSION = "1.2";
    private static SimpleChannel channel;
    private final PacketType<?> packetType;
    <T extends SimplePacketBase> ControlCraftPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
                                            NetworkDirection direction) {
        packetType = new PacketType<>(type, factory, direction);
    }

    public static void registerPackets() {
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ControlCraft.MODID, ControlCraft.MODID+"_channel")).networkProtocolVersion(() -> {
                    return NETWORK_VERSION;
                })
                .clientAcceptedVersions(NETWORK_VERSION::equals).serverAcceptedVersions(NETWORK_VERSION::equals).simpleChannel();

        for (ControlCraftPackets packet : values())
            packet.packetType.register();
    }

    public static SimpleChannel getChannel() {
        return channel;
    }

    public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
        getChannel().send(
                PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), range, world.dimension())),
                message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), message);
    }


    private static class PacketType<T extends SimplePacketBase> {
        private static int index = 0;

        private final BiConsumer<T, FriendlyByteBuf> encoder;
        private final Function<FriendlyByteBuf, T> decoder;
        private final BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
        private final Class<T> type;
        private final NetworkDirection direction;

        private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
            encoder = T::write;
            decoder = factory;
            handler = (packet, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                if (packet.handle(context)) {
                    context.setPacketHandled(true);
                }
            };
            this.type = type;
            this.direction = direction;
        }

        private void register() {
            getChannel().messageBuilder(type, index++, direction)
                    .encoder(encoder)
                    .decoder(decoder)
                    .consumerNetworkThread(handler)
                    .add();
        }
    }
}
