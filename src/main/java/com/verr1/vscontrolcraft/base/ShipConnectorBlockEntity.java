package com.verr1.vscontrolcraft.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class ShipConnectorBlockEntity extends OnShipBlockEntity{

    private long companionShipID;
    private Direction companionShipDirection;

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
        ServerShip companionShip = shipWorldCore.getLoadedShips().getById(companionShipID);
        return companionShip;
    }

    public void setCompanionShipDirection(Direction direction){
        if(direction == null)return;
        this.companionShipDirection = direction;
    }

    public Direction getCompanionShipDirection(){
        return companionShipDirection;
    }

}
