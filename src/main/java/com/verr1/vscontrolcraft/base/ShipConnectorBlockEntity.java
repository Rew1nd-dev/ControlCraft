package com.verr1.vscontrolcraft.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class ShipConnectorBlockEntity extends OnShipDirectinonalBlockEntity {

    private long companionShipID;
    private Direction companionShipDirection = Direction.UP;

    public ShipConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected void setCompanionShipID(long companionShipID) {
        this.companionShipID = companionShipID;
    }

    protected long getCompanionShipID() {
        return this.companionShipID;
    }

    public ServerShip getCompanionServerShip(){
        if(level.isClientSide)return null;
        var shipWorldCore = VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);
        return shipWorldCore.getLoadedShips().getById(companionShipID);
    }

    public void setCompanionShipDirection(Direction direction){
        if(direction == null)return;
        this.companionShipDirection = direction;
    }

    public Direction getCompanionShipDirection(){
        return companionShipDirection;
    }

    public boolean hasCompanionShip(){
        return getCompanionServerShip() != null;
    }

    /*
    public void initializeCompanionShipID(Collection<ConstrainKey> keys){
        for (var k : keys){
            if(!ConstrainCenter.isRegistered(k))return;
        }
        if(keys.isEmpty())return;
        ConstrainKey any = keys.stream().toList().getFirst();
        if(ConstrainCenter.has(any))return;
        VSConstraint c = Objects.requireNonNull(ConstrainCenter.get(any));
        long s1 = c.getShipId0();
        long s2 = c.getShipId1();
        if(s1 == getServerShipID())companionShipID = s2;
        if(s2 == getServerShipID())companionShipID = s1;
    }
    * */



    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if(clientPacket)return;
        try{
            companionShipID = tag.getLong("companionShipID");
            companionShipDirection = Direction.valueOf(tag.getString("companionShipDirection"));
        }catch (Exception e){
            clearCompanionShipInfo();
        }

    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(clientPacket)return;
        tag.putLong("companionShipID", companionShipID);
        tag.putString("companionShipDirection", companionShipDirection.name());
    }

    public void clearCompanionShipInfo(){
        setCompanionShipID(-1);
        setCompanionShipDirection(Direction.UP);
    }

}
