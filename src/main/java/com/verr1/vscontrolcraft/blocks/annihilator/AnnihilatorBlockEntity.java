package com.verr1.vscontrolcraft.blocks.annihilator;

import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.base.OnShipBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class AnnihilatorBlockEntity extends OnShipBlockEntity {
    public AnnihilatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    public void annihilate(){
        if(level.isClientSide)return;
        if(!isOnServerShip())return;
        DeferralExecutor.executeLater(
                () -> {
                    ServerShip ship = getServerShipOn();
                    if(ship == null)return;
                    var shipWorldCore = VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);
                    shipWorldCore.deleteShip(ship);
                },
                60
        );
    }



}
