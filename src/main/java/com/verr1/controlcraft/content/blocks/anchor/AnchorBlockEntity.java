package com.verr1.controlcraft.content.blocks.anchor;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.gui.AnchorScreen;
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

    public double airResistance = 0;
    public double extraGravity = 0;
    public double rotationalResistance = 0;

    public AnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(() -> airResistance, a -> airResistance = a, SerializeUtils.DOUBLE, "airResistance"), Side.SERVER
        );
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(() -> extraGravity, a -> extraGravity = a, SerializeUtils.DOUBLE, "extraGravity"), Side.SERVER
        );
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(() -> rotationalResistance, a -> rotationalResistance = a, SerializeUtils.DOUBLE, "rotationalResistance"), Side.SERVER
        );
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
                    ScreenOpener.open(new AnchorScreen(packet.getBoundPos(), a, e, r)
            ));
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
        return new LogicalAnchor(airResistance, extraGravity, rotationalResistance);
    }



}
