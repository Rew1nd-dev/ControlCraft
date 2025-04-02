package com.verr1.controlcraft.foundation.vsapi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.core.impl.game.ships.ShipData;
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;

import java.util.List;

import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toJOML;


public class ShipAssembler {
    public static ShipAssembler INSTANCE = new ShipAssembler();

    public ServerShip assembleToShip(
            ServerLevel level,
            BlockPos block,
            boolean removeOriginal,
            double scale,
            boolean shouldDisableSplitting)
    {
        if(level.getBlockState(block).isAir())return null;
        DenseBlockPosSet blockPosSet = new DenseBlockPosSet();
        blockPosSet.add(new Vector3i(block.getX(), block.getY(), block.getZ()));
        ServerShip ship = ShipAssemblyKt.createNewShipWithBlocks(
                block,
                blockPosSet,
                level
        );

        return ship;
    }


}
