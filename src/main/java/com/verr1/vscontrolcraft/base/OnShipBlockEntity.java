package com.verr1.vscontrolcraft.base;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class OnShipBlockEntity extends SmartBlockEntity {
    public OnShipBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    public static void updateBlockState(Level world, BlockPos pos, BlockState newState){
        world.setBlock(pos, newState, 3);
    }

    public Vector3d getDirectionJOML() {
        return Util.Vec3itoVector3d(getDirection().getNormal());
    }

    public Direction getDirection(){
        return getBlockState().getValue(BlockStateProperties.FACING);
    }


    public boolean isOnServerShip(){
        return getServerShipOn() != null;
    }

    public @Nullable ServerShip getServerShipOn(){
        if(level.isClientSide)return null;
        ServerShip ship = VSGameUtilsKt.getShipManagingPos((ServerLevel) level, getBlockPos());
        return ship;
    }

    public Quaterniondc getSelfShipQuaternion(){
        Quaterniond q = new Quaterniond();
        Optional.ofNullable(getServerShipOn()).ifPresent((serverShip -> serverShip.getTransform().getShipToWorldRotation().get(q)));
        return q;
    }

    public String getDimensionID(){
        return VSGameUtilsKt.getDimensionId(level);
    }

    public long getServerShipID(){
        ServerShip ship = getServerShipOn();
        if(ship != null)return ship.getId();
        String dimensionID = getDimensionID();
        ShipObjectServerWorld sosw = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);
        return sosw.getDimensionToGroundBodyIdImmutable().get(dimensionID);
    }
}
