package com.verr1.vscontrolcraft.blocks.anchor;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.compat.valkyrienskies.anchor.AnchorForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.anchor.LogicalAnchor;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.valkyrienskies.core.api.ships.ServerShip;

public class AnchorBlockEntity extends OnShipDirectinonalBlockEntity implements
        IPacketHandler
{



    private double airResistance = 0;
    private double extraGravity = 0;


    public AnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setAirResistance(double airResistance) {
        this.airResistance = airResistance;
    }

    public void setExtraGravity(double extraGravity) {
        this.extraGravity = extraGravity;
    }

    public double getAirResistance() {
        return airResistance;
    }

    public double getExtraGravity() {
        return extraGravity;
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide)return;
        syncAttachInducer();
    }

    public void syncAttachInducer(){
        if(level.isClientSide)return;
        ServerShip ship = getServerShipOn();
        if(ship == null)return;
        var inducer = AnchorForceInducer.getOrCreate(ship);
        inducer.update(new LevelPos(getBlockPos(), (ServerLevel) getLevel()));
    }

    public LogicalAnchor getLogicalAnchor() {
        return new LogicalAnchor(airResistance, extraGravity);
    }

    public void displayScreen(ServerPlayer player){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN)
                .withDouble(airResistance)
                .withDouble(extraGravity)
                .build();

        AllPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN){
            airResistance = packet.getDoubles().get(0);
            extraGravity = packet.getDoubles().get(1);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new AnchorScreen(packet.getBoundPos(), airResistance, extraGravity)
            ));
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING){
            airResistance = packet.getDoubles().get(0);
            extraGravity = packet.getDoubles().get(1);
        }
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if(clientPacket)return;
        compound.putDouble("extra_gravity", extraGravity);
        compound.putDouble("air_resistance", airResistance);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if(clientPacket)return;
        extraGravity = compound.getDouble("extra_gravity");
        airResistance = compound.getDouble("air_resistance");
    }
}
