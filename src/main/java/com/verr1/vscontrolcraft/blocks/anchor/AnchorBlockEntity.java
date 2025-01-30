package com.verr1.vscontrolcraft.blocks.anchor;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.OnShipBlockEntity;
import com.verr1.vscontrolcraft.compat.valkyrienskies.anchor.AnchorForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.anchor.LogicalAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.core.api.ships.ServerShip;

public class AnchorBlockEntity extends OnShipBlockEntity {



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
}
