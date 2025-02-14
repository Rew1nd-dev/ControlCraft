package com.verr1.vscontrolcraft.blocks.magnet;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.MagnetPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.magnet.LogicalMagnet;
import com.verr1.vscontrolcraft.compat.valkyrienskies.magnet.MagnetForceInducer;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

public class MagnetBlockEntity extends SmartBlockEntity implements
        IPacketHandler
{

    private final SynchronizedField<ShipPhysics> physics = new SynchronizedField<>(ShipPhysics.EMPTY);


    private double strength = 300;

    private MagnetPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private boolean canBeRedstonePowered = false;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new MagnetPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public void writePhysicsShipInfo(ShipPhysics sp){
        physics.write(sp);
    }

    public ShipPhysics readPhysicsShipInfo(){
        return physics.read();
    }

    public MagnetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Vector3d getFaceCenter(){
        Vector3d dir = Util.Vec3itoVector3d(getBlockState().getValue(MagnetBlock.FACING).getNormal()) ;
        Vector3d f_sc = Util.Vec3toVector3d(getBlockPos().getCenter()).fma(0.0, dir);
        return f_sc;
    }


    public Vector3dc getPosition_wc(){
        if(level.isClientSide)return new Vector3d();
        Vector3d p_sc = getFaceCenter();
        ServerShip ship = VSGameUtilsKt.getShipManagingPos((ServerLevel) level, getBlockPos());
        if(ship == null)return p_sc;

        return readPhysicsShipInfo().s2wTransform().transformPosition(p_sc);
    }

    public boolean isOnServerShip(){
        if(level.isClientSide)return false;
        return VSGameUtilsKt.getShipManagingPos((ServerLevel) level, getBlockPos()) != null;
    }

    public Vector3d getDirection(){
        Vector3d dir = Util.Vec3itoVector3d(getBlockState().getValue(MagnetBlock.FACING).getNormal());
        if(!isOnServerShip())return dir;
        return readPhysicsShipInfo().s2wTransform().transformDirection(dir);
    }

    public Vector3d getRelativePosition(){
        if(getServerShipOn() == null)return new Vector3d();
        Vector3d p_sc = getFaceCenter();
        if(isOnServerShip()) return p_sc.sub(getServerShipOn().getInertiaData().getCenterOfMassInShip());
        return new Vector3d();
    }

    public @Nullable ServerShip getServerShipOn(){
        if(level.isClientSide)return null;
        return VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, getBlockPos());
    }

    public double getStrength(){
        return strength;
    }

    public void setStrength(double strength){
        this.strength = strength;
    }

    public void syncMagnetManager(){
        MagnetManager.activate(new LogicalMagnet(getBlockPos(), (ServerLevel)level));
    }

    public void syncMagnetInducer(){
        ServerShip ship = getServerShipOn();
        if(ship == null)return;
        MagnetForceInducer inducer = MagnetForceInducer.getOrCreate(ship);
        inducer.update(new LevelPos(getBlockPos(), (ServerLevel)level));
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide)return;
        syncMagnetManager();
        syncMagnetInducer();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    public LogicalMagnet getLogicalMagnet() {
        return new LogicalMagnet(getBlockPos(), (ServerLevel)level);
    }

    protected void displayScreen(ServerPlayer player){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SETTING_0)
                .withDouble(getStrength())
                .build();
        AllPackets.sendToPlayer(
                p,
                player
        );

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING_0){
            double strength = packet.getDoubles().get(0);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ScreenOpener.open(new MagnetScreen(packet.getBoundPos(), strength));
            });
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING_0){
            double strength = packet.getDoubles().get(0);
            setStrength(strength);
        }
    }
}
