package com.verr1.controlcraft.foundation.network.handler;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.content.blocks.motor.AbstractMotorBlockEntity;
import com.verr1.controlcraft.content.blocks.slider.SliderBlockEntity;
import com.verr1.controlcraft.foundation.ServerBlockEntityGetter;
import com.verr1.controlcraft.foundation.api.IBruteConnectable;
import com.verr1.controlcraft.foundation.api.IConstraintHolder;
import com.verr1.controlcraft.foundation.api.IControllerProvider;
import com.verr1.controlcraft.foundation.api.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.executor.FaceAlignmentSchedule;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldMessage;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.ExposedFieldOpenScreenPacket;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

public class ServerGenericPacketHandler {

    public static void dispatchPacket(GenericServerPacket packet, NetworkEvent.Context context) {
        switch (packet.getType()){
            case GENERIC_REQUEST_EXPOSED_FIELDS : handleRequestExposedFields(packet, context);break;
            case GENERIC_RESET_EXPOSED_FIELDS : handleResetExposedFields(packet, context); break;
            case GENERIC_CONTROLLER_SETTING: handleControllerSettings(packet, context); break;
            case GENERIC_CYCLE_CONTROLLER_MODE: handleCycleControllerMode(packet, context);break;
            case DESTROY_CONSTRAIN: handleDestroyConstraints(packet, context); break;
            case DESTROY_ALL_CONSTRAIN: handleDestroyAllConstraints(packet, context); break;
            case BRUTE_CONNECT: handleBruteConnect(packet, context); break;
            case CONNECT: handleConnect(packet, context); break;
            default: break;
        }
    }

    public static void handleBruteConnect(GenericServerPacket packet, NetworkEvent.Context context){
        BlockPos basePos = BlockPos.of(packet.getLongs().get(0));
        Direction baseAlign = Direction.values()[packet.getLongs().get(1).intValue()];
        Direction baseForward = Direction.values()[packet.getLongs().get(2).intValue()];
        BlockPos slavePos = BlockPos.of(packet.getLongs().get(3));
        Direction slaveAlign = Direction.values()[packet.getLongs().get(4).intValue()];
        Direction slaveForward = Direction.values()[packet.getLongs().get(5).intValue()];

         Optional
            .ofNullable(context.getSender())
            .map(e -> ServerBlockEntityGetter.INSTANCE.getBlockEntityAt(e.serverLevel(), basePos, IBruteConnectable.class))
            .map(Optional::orElseThrow)
            .ifPresent(b -> b.bruteDirectionalConnectWith(slavePos, slaveAlign, slaveForward));
    }

    public static void handleConnect(GenericServerPacket packet, NetworkEvent.Context context){
        try{
            BlockPos basePos = BlockPos.of(packet.getLongs().get(0));
            Direction baseAlign = Direction.values()[packet.getLongs().get(1).intValue()];
            Direction baseForward = Direction.values()[packet.getLongs().get(2).intValue()];
            BlockPos slavePos = BlockPos.of(packet.getLongs().get(3));
            Direction slaveAlign = Direction.values()[packet.getLongs().get(4).intValue()];
            Direction slaveForward = Direction.values()[packet.getLongs().get(5).intValue()];

            Runnable expiredTask =
                    () -> Optional
                            .ofNullable(context.getSender())
                            .map(e -> ServerBlockEntityGetter.INSTANCE.getBlockEntityAt(e.serverLevel(), basePos, IBruteConnectable.class))
                            .map(Optional::orElseThrow)
                            .ifPresent(b -> b.bruteDirectionalConnectWith(slavePos, slaveAlign, slaveForward));

            Optional
                    .ofNullable(context.getSender())
                    .map(ServerPlayer::serverLevel)
                    .map(level -> new FaceAlignmentSchedule
                                        .builder(basePos, baseAlign, slavePos, slaveAlign, level)
                                        .withGivenYForward(baseForward)
                                        .withGivenYForward(slaveForward)
                                        .withTimeBeforeExpired(0)
                                        .withOnExpiredTask(expiredTask)
                                        .build()
                    )
                    .ifPresent(
                            ControlCraftServer.SERVER_INTERVAL_EXECUTOR::executeOnSchedule
                    );
        }catch (IndexOutOfBoundsException e){
            ControlCraft.LOGGER.info("Invalid packet of wrong Direction enum index received");
        }

    }

    public static void handleDestroyAllConstraints(GenericServerPacket packet, NetworkEvent.Context context){
        // currently does nothing
    }

    public static void handleDestroyConstraints(GenericServerPacket packet, NetworkEvent.Context context){
        BlockPos pos = BlockPos.of(packet.getLongs().get(0));
        Optional
                .ofNullable(context.getSender()).map(e -> ServerBlockEntityGetter.INSTANCE.getBlockEntityAt(e.serverLevel(), pos, IConstraintHolder.class))
                .map(Optional::orElseThrow)
                .ifPresent(IConstraintHolder::destroyConstraints);
    }

    public static void handleCycleControllerMode(GenericServerPacket packet, NetworkEvent.Context context){
        BlockPos pos = BlockPos.of(packet.getLongs().get(0));
        Optional
                .ofNullable(context.getSender()).map(e -> e.level().getExistingBlockEntity(pos))
                .filter(AbstractMotorBlockEntity.class::isInstance)
                .map(AbstractMotorBlockEntity.class::cast)
                .ifPresent(AbstractMotorBlockEntity::toggleMode);

        Optional
                .ofNullable(context.getSender()).map(e -> e.level().getExistingBlockEntity(pos))
                .filter(SliderBlockEntity.class::isInstance)
                .map(SliderBlockEntity.class::cast)
                .ifPresent(SliderBlockEntity::toggleMode);
    }

    public static void handleControllerSettings(GenericServerPacket packet, NetworkEvent.Context context){
        BlockPos pos = BlockPos.of(packet.getLongs().get(0));
        double p = packet.getDoubles().get(0);
        double i = packet.getDoubles().get(1);
        double d = packet.getDoubles().get(2);
        double value = packet.getDoubles().get(3);
        Optional
                .ofNullable(context.getSender())
                .map(e -> ServerBlockEntityGetter.INSTANCE.getBlockEntityAt(e.serverLevel(), pos, IControllerProvider.class))
                .map(Optional::orElseThrow)
                .ifPresent(c -> c.getController().setParameter(p, i, d).setTarget(value));
    }

    public static void handleResetExposedFields(GenericServerPacket packet, NetworkEvent.Context context){
        BlockPos pos = BlockPos.of(packet.getLongs().get(0));
        BlockEntity be = context.getSender().level().getExistingBlockEntity(pos);
        if(be instanceof ITerminalDevice device){
            device.reset();
        }
    }

    public static void handleRequestExposedFields(GenericServerPacket packet, NetworkEvent.Context context) {
        BlockPos pos = BlockPos.of(packet.getLongs().get(0));
        BlockEntity be = context.getSender().level().getExistingBlockEntity(pos);
        if(be instanceof ITerminalDevice device){
            var availableFields = device
                    .fields()
                    .stream()
                    .map(e -> new ExposedFieldMessage(
                                    e.type,
                                    e.min_max.get(true),
                                    e.min_max.get(false),
                                    e.directionOptional
                            )
                    )
                    .toList();
            ControlCraftPackets.sendToPlayer(
                    new ExposedFieldOpenScreenPacket(
                            availableFields,
                            pos
                    ),
                    context.getSender()
            );
        }
    }

}
