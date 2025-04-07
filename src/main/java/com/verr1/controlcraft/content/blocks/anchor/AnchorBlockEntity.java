package com.verr1.controlcraft.content.blocks.anchor;


import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.foundation.type.Side;
import com.verr1.controlcraft.content.gui.legacy.AnchorScreen;
import com.verr1.controlcraft.content.valkyrienskies.attachments.AnchorForceInducer;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.foundation.data.logical.LogicalAnchor;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

public class AnchorBlockEntity extends OnShipBlockEntity
    implements IPacketHandler
{

    public static NetworkKey AIR_RESISTANCE = NetworkKey.create("air_resistance");
    public static NetworkKey EXTRA_GRAVITY = NetworkKey.create("extra_gravity");
    public static NetworkKey ROTATIONAL_RESISTANCE = NetworkKey.create("rotational_resistance");
    public static NetworkKey RESISTANCE_AT_POS =  NetworkKey.create("air_resistance_at_pos");
    public static NetworkKey GRAVITY_AT_POS =  NetworkKey.create("extra_gravity_at_pos");

    public double getAirResistance() {
        return airResistance;
    }

    public void setAirResistance(double airResistance) {
        this.airResistance = airResistance;
    }

    public double getExtraGravity() {
        return extraGravity;
    }

    public void setExtraGravity(double extraGravity) {
        this.extraGravity = extraGravity;
    }

    public double getRotationalResistance() {
        return rotationalResistance;
    }

    public void setRotationalResistance(double rotationalResistance) {
        this.rotationalResistance = rotationalResistance;
    }

    public double airResistance = 0;
    public double extraGravity = 0;
    public double rotationalResistance = 0;
    public boolean airResistanceAtPos = false;

    public boolean isExtraGravityAtPos() {
        return extraGravityAtPos;
    }

    public void setExtraGravityAtPos(boolean extraGravityAtPos) {
        this.extraGravityAtPos = extraGravityAtPos;
    }

    public boolean isAirResistanceAtPos() {
        return airResistanceAtPos;
    }

    public void setAirResistanceAtPos(boolean airResistanceAtPos) {
        this.airResistanceAtPos = airResistanceAtPos;
    }

    public boolean extraGravityAtPos = false;

    public AnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        /*
       registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(this::getAirResistance, this::setAirResistance, SerializeUtils.DOUBLE, AIR_RESISTANCE), Side.SHARED
        );
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(this::getExtraGravity, this::setExtraGravity, SerializeUtils.DOUBLE, EXTRA_GRAVITY), Side.SHARED
        );
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(this::getRotationalResistance, this::setExtraGravity, SerializeUtils.DOUBLE, ROTATIONAL_RESISTANCE), Side.SHARED
        );
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(this::isAirResistanceAtPos, this::setAirResistanceAtPos, SerializeUtils.BOOLEAN, RESISTANCE_AT_POS), Side.SHARED
        );
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(this::isExtraGravityAtPos, this::setExtraGravityAtPos, SerializeUtils.BOOLEAN, GRAVITY_AT_POS), Side.SHARED
        );
        * */
        buildRegistry(AIR_RESISTANCE)
                .withBasic(SerializePort.of(this::getAirResistance, this::setAirResistance, SerializeUtils.DOUBLE))
                .withClient(new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class)).register();

        buildRegistry(EXTRA_GRAVITY)
                .withBasic(SerializePort.of(this::getExtraGravity, this::setExtraGravity, SerializeUtils.DOUBLE))
                .withClient(new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class)).register();

        buildRegistry(ROTATIONAL_RESISTANCE)
                .withBasic(SerializePort.of(this::getRotationalResistance, this::setRotationalResistance, SerializeUtils.DOUBLE))
                .withClient(new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class)).register();

        buildRegistry(RESISTANCE_AT_POS)
                .withBasic(SerializePort.of(this::isAirResistanceAtPos, this::setAirResistanceAtPos, SerializeUtils.BOOLEAN))
                .withClient(new ClientBuffer<>(SerializeUtils.BOOLEAN, Boolean.class)).register();

        buildRegistry(GRAVITY_AT_POS)
                .withBasic(SerializePort.of(this::isExtraGravityAtPos, this::setExtraGravityAtPos, SerializeUtils.BOOLEAN))
                .withClient(new ClientBuffer<>(SerializeUtils.BOOLEAN, Boolean.class)).register();


    }

    @Override
    public void tickServer() {
        syncAttachInducer();
    }

    public void syncAttachInducer(){
        if(level != null && level.isClientSide)return;
        Optional
            .ofNullable(getLoadedServerShip())
            .map(AnchorForceInducer::getOrCreate)
            .ifPresent(inducer -> inducer.alive(WorldBlockPos.of(level, getBlockPos())));
    }

    public void displayScreen(ServerPlayer player){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(airResistance)
                .withDouble(extraGravity)
                .withDouble(rotationalResistance)
                .build();

        ControlCraftPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == RegisteredPacketType.OPEN_SCREEN_0){
            var a = packet.getDoubles().get(0);
            var e = packet.getDoubles().get(1);
            var r = packet.getDoubles().get(2);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new AnchorScreen(packet.getBoundPos(), a, e, r))
                    // ScreenOpener.open(new ExampleGui())
                    // Minecraft.getInstance().setScreen(new ScreenUIRenderer(new ExampleDynamic()))
            );
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == RegisteredPacketType.SETTING_0){
            airResistance = packet.getDoubles().get(0);
            extraGravity = packet.getDoubles().get(1);
            rotationalResistance = packet.getDoubles().get(2);
        }
    }

    public LogicalAnchor getLogicalAnchor() {
        return new LogicalAnchor(
                airResistance,
                extraGravity,
                rotationalResistance,
                WorldBlockPos.of(level, getBlockPos()),
                isAirResistanceAtPos(),
                isExtraGravityAtPos()
        );
    }



}
