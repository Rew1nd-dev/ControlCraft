package com.verr1.vscontrolcraft.registry;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Hinge.packets.*;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerCycleModePacket;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.DirectionalAssemblePacket;
import com.verr1.vscontrolcraft.blocks.servoMotor.SimpleAssemblePacket;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerOpenScreenPacket;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerSettingsPacket;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialConnectPacket;
import com.verr1.vscontrolcraft.blocks.terminal.TerminalSettingsPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
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

public enum AllPackets {
    // Client To Server
    // NETWORK_ID_SETTING(ReceiverRegisterPacket.class, ReceiverRegisterPacket::new, NetworkDirection.PLAY_TO_SERVER),
    // PROPELLER_SETTINGS(PropellerSettingsPacket.class, PropellerSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    PID_SETTINGS(PIDControllerSettingsPacket.class, PIDControllerSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    // MAGNET_SETTINGS(MagnetSettingsPacket.class, MagnetSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    // ANCHOR_SETTINGS(AnchorSettingsPacket.class, AnchorSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    // SPATIAL_SETTINGS(SpatialSettingsPacket.class, SpatialSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    TERMINAL_SETTINGS(TerminalSettingsPacket.class, TerminalSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    EXPOSED_FIELD_SETTINGS(ExposedFieldSettingsPacket.class, ExposedFieldSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
    EXPOSED_FIELD_RESET(ExposedFieldResetPacket.class, ExposedFieldResetPacket::new, NetworkDirection.PLAY_TO_SERVER),
    // SPINALYZER_TARGET_SELECTION(SpinalyzerLinkPacket.class, SpinalyzerLinkPacket::new, NetworkDirection.PLAY_TO_SERVER),
    DESTROY_ALL_CONSTRAINT(DestroyAllConstrainPacket.class, DestroyAllConstrainPacket::new, NetworkDirection.PLAY_TO_SERVER),
    SERVOMOTOR_CONSTRAIN_ASSEMBLE(SimpleAssemblePacket.class, SimpleAssemblePacket::new, NetworkDirection.PLAY_TO_SERVER),
    JOINTMOTOR_CONSTRAIN_ASSEMBLE(DirectionalAssemblePacket.class, DirectionalAssemblePacket::new, NetworkDirection.PLAY_TO_SERVER),
    HINGE_BRUTE_CONNECTION(HingeBruteConnectPacket.class, HingeBruteConnectPacket::new, NetworkDirection.PLAY_TO_SERVER),
    ANCHOR_CONNECTION(SpatialConnectPacket.class, SpatialConnectPacket::new, NetworkDirection.PLAY_TO_SERVER),
    HINGE_FLIP(HingeFlipPacket.class, HingeFlipPacket::new, NetworkDirection.PLAY_TO_SERVER),
    DESTROY_CONSTRAIN(DestroyConstrainPacket.class, DestroyConstrainPacket::new, NetworkDirection.PLAY_TO_SERVER),
    SERVO_TOGGLE_MODE(PIDControllerCycleModePacket.class, PIDControllerCycleModePacket::new, NetworkDirection.PLAY_TO_SERVER),
    EXPOSED_FIELD_REQUEST(ExposedFieldRequestPacket.class, ExposedFieldRequestPacket::new, NetworkDirection.PLAY_TO_SERVER),
    BLOCK_BOUND_SERVER(BlockBoundServerPacket.class, BlockBoundServerPacket::new, NetworkDirection.PLAY_TO_SERVER),
    HINGE_ADJUSTMENT(HingeAdjustLevelPacket.class, HingeAdjustLevelPacket::new, NetworkDirection.PLAY_TO_SERVER),

    //Server To Client
    // RECEIVER_SCREEN_OPEN(ReceiverOpenScreenPacket.class, ReceiverOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // PROPELLER_SCREEN_OPEN(PropellerOpenScreenPacket.class, PropellerOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    PID_SCREEN_OPEN(PIDControllerOpenScreenPacket.class, PIDControllerOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    EXPOSED_FIELD_SYNC_CLIENT(ExposedFieldSyncClientPacket.class, ExposedFieldSyncClientPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // MAGNET_SCREEN_OPEN(MagnetOpenScreenPacket.class, MagnetOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // ANCHOR_SCREEN_OPEN(AnchorOpenScreenPacket.class, AnchorOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // SPATIAL_SCREEN_OPEN(SpatialOpenScreenPacket.class, SpatialOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    EXPOSED_FIELD_SCREEN_OPEN(ExposedFieldOpenScreenPacket.class, ExposedFieldOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // TERMINAL_SCREEN_OPEN(TerminalOpenScreenPacket.class, TerminalOpenScreenPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    BLOCK_BOUND_CLIENT(BlockBoundClientPacket.class, BlockBoundClientPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // SYNC_SERVOMOTOR_ANIMATION(ServoMotorSyncAnimationPacket.class, ServoMotorSyncAnimationPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // SYNC_PROPELLER_ANIMATION(PropellerSyncAnimationPacket.class, PropellerSyncAnimationPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // SYNC_WING_CONTROLLER_ANIMATION(WingControllerSyncAnimationPacket.class, WingControllerSyncAnimationPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // SYNC_SLIDER_CONTROLLER_ANIMATION(SliderSyncAnimationPacket.class, SliderSyncAnimationPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    // SYNC_JET_RUDDER_ANIMATION(JetRudderSyncAnimationPacket.class, JetRudderSyncAnimationPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    SYNC_HINGE_LEVEL(HingeSyncClientPacket.class, HingeSyncClientPacket::new, NetworkDirection.PLAY_TO_CLIENT);

    public static final String NETWORK_VERSION = "1.2";

    private static SimpleChannel channel;

    private PacketType<?> packetType;

    <T extends SimplePacketBase> AllPackets(Class<T> type, Function<FriendlyByteBuf, T> factory,
                                            NetworkDirection direction) {
        packetType = new PacketType<>(type, factory, direction);
    }

    public static void registerPackets() {
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ControlCraft.MODID, ControlCraft.MODID+"_channel")).networkProtocolVersion(() -> {
                    return NETWORK_VERSION;
                })
                .clientAcceptedVersions(NETWORK_VERSION::equals).serverAcceptedVersions(NETWORK_VERSION::equals).simpleChannel();

        for (AllPackets packet : values())
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

        private BiConsumer<T, FriendlyByteBuf> encoder;
        private Function<FriendlyByteBuf, T> decoder;
        private BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
        private Class<T> type;
        private NetworkDirection direction;

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
