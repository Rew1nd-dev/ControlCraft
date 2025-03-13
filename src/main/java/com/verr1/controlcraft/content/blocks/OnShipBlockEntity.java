package com.verr1.controlcraft.content.blocks;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

public class OnShipBlockEntity extends KineticBlockEntity {


    private final ArrayList<SerializeUtils.ReadWriter<?>> serverReadWriter = new ArrayList<>();
    private final ArrayList<SerializeUtils.ReadWriter<?>> clientReadWriter = new ArrayList<>();
    private final ArrayList<SerializeUtils.ReadWriter<?>> commonReadWriter = new ArrayList<>();

    private final ArrayList<SerializeUtils.ReadWriteExecutor> serverReadWriteExecutor = new ArrayList<>();
    private final ArrayList<SerializeUtils.ReadWriteExecutor> clientReadWriteExecutor = new ArrayList<>();
    private final ArrayList<SerializeUtils.ReadWriteExecutor> commonReadWriteExecutor = new ArrayList<>();

    protected void registerFieldReadWriter(SerializeUtils.ReadWriter<?> rw, Side side){
        switch (side){
            case SERVER -> serverReadWriter.add(rw);
            case CLIENT -> clientReadWriter.add(rw);
            case COMMON -> commonReadWriter.add(rw);
        }
    }

    protected void registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor executor, Side side){
        switch (side){
            case SERVER -> serverReadWriteExecutor.add(executor);
            case CLIENT -> clientReadWriteExecutor.add(executor);
            case COMMON -> commonReadWriteExecutor.add(executor);
        }
    }


    public OnShipBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Vector3d getDirectionJOML() {
        return ValkyrienSkies.set(new Vector3d(), getDirection().getNormal());
    }

    public Direction getDirection(){
        if(getBlockState().hasProperty(BlockStateProperties.FACING)) return getBlockState().getValue(BlockStateProperties.FACING);
        return Direction.UP;
    }


    public boolean isOnShip(){
        return getShipOn() != null;
    }

    public @Nullable LoadedServerShip getLoadedServerShip(){
        if(level == null || level.isClientSide)return null;
        return Optional
                .ofNullable(ValkyrienSkies.getShipWorld(level.getServer()))
                .map((shipWorld -> shipWorld.getLoadedShips().getById(getShipID()))).orElse(null);
    }

    public @Nullable Ship getShipOn(){
        return ValkyrienSkies.getShipManagingBlock(level, getBlockPos());
    }

    public Quaterniondc getSelfShipQuaternion(){
        Quaterniond q = new Quaterniond();
        Optional.ofNullable(getShipOn()).ifPresent((serverShip -> serverShip.getTransform().getShipToWorldRotation().get(q)));
        return q;
    }

    public void tickServer(){

    }

    public void tickClient(){

    }


    public void tickCommon(){

    }

    public void lazyTickServer(){

    }

    public void lazyTickClient(){

    }


    public void lazyTickCommon(){

    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        lazyTickCommon();
        if(level != null && level.isClientSide)lazyTickClient();
        else lazyTickServer();
    }

    public void tick(){
        super.tick();
        tickCommon();
        if(level != null && level.isClientSide)tickClient();
        else tickServer();
    }

    public String getDimensionID(){
        if(level == null)return "";
        return ValkyrienSkies.getDimensionId(level);
    }

    public long getShipID(){
        Ship ship = getShipOn();
        if(ship != null)return ship.getId();
        return -1L;
        /*
        * if(level == null)
        String dimensionID = getDimensionID();
        ServerShipWorld ssw = ValkyrienSkies.api().getServerShipWorld(level.getServer());
        return
        * */

    }


    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        commonReadWriter.forEach(rw -> rw.onRead(compound));
        if(clientPacket)clientReadWriter.forEach(rw -> rw.onRead(compound));
        else serverReadWriter.forEach(rw -> rw.onRead(compound));

        commonReadWriteExecutor.forEach(e -> e.onRead(compound));
        if(clientPacket)clientReadWriteExecutor.forEach(e -> e.onRead(compound));
        else serverReadWriteExecutor.forEach(e -> e.onRead(compound));
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        commonReadWriter.forEach(rw -> rw.onWrite(compound));
        if(clientPacket)clientReadWriter.forEach(rw -> rw.onWrite(compound));
        else serverReadWriter.forEach(rw -> rw.onWrite(compound));

        commonReadWriteExecutor.forEach(e -> e.onWrite(compound));
        if(clientPacket)clientReadWriteExecutor.forEach(e -> e.onWrite(compound));
        else serverReadWriteExecutor.forEach(e -> e.onWrite(compound));
    }

    public enum Side{
        SERVER,
        CLIENT,
        COMMON
    }
}
