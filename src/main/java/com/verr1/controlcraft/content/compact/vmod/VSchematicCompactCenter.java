package com.verr1.controlcraft.content.compact.vmod;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.content.blocks.motor.AbstractMotor;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.Ship;

import java.util.Map;

public class VSchematicCompactCenter {

    public static @Nullable CompoundTag PreWriteMotorVModCompact(AbstractMotor motor){
        CompoundTag motorOriginal = new CompoundTag();
        CompoundTag compact = new CompoundTag();
        motor.writeCompact(motorOriginal);
        Ship comp = motor.getCompanionServerShip();
        if(comp == null || motor.blockConnectContext().equals(BlockPos.ZERO))return null;
        int xM = motor.blockConnectContext().getX();
        int zM = motor.blockConnectContext().getZ();
        ControlCraft.LOGGER.info("PreWriteMotorVModCompact: " + xM + " " + zM);
        BlockPos center_xz_old = centerPosOf(xM, zM);
        compact.putLong("o_comp_chunk_center", center_xz_old.asLong());
        compact.putLong("o_comp_ID", comp.getId());

        motorOriginal.put("compact", compact);
        return motorOriginal;
    }

    public static CompoundTag PreMotorReadVModCompact(
            @NotNull ServerLevel serverLevel,
            @NotNull Map<Long, Long> map,
            @Nullable CompoundTag tagToModify
    ){

        ControlCraft.LOGGER.info("PreMotorReadVModCompact: " + tagToModify);
        ControlCraft.LOGGER.info("Map: " + map);

        if(tagToModify == null)return null;
        CompoundTag compact = tagToModify.getCompound("compact");
        long o_comp_id = compact.getLong("o_comp_ID");
        if(!map.containsKey(o_comp_id))return tagToModify;
        long n_comp_id = map.get(o_comp_id);

        ControlCraft.LOGGER.info("has n ship {}", n_comp_id);

        Ship n_comp = ValkyrienSkies.getShipWorld(serverLevel).getAllShips().getById(n_comp_id);
        if(n_comp == null)return tagToModify;

        ControlCraft.LOGGER.info("found n ship");

        int xM = n_comp.getChunkClaim().getXMiddle() * 16;
        int zM = n_comp.getChunkClaim().getZMiddle() * 16;

        BlockPos center_xz_new = centerPosOf(xM, zM);

        tagToModify.getCompound("compact").putLong("n_comp_chunk_center", center_xz_new.asLong());

        ControlCraft.LOGGER.info("PreMotorReadVModCompact Modified: " + tagToModify);
        return tagToModify;
    }


    public static void PostMotorReadVModCompact(AbstractMotor motor, CompoundTag tag){
        CompoundTag compact = tag.getCompound("compact");

        ControlCraft.LOGGER.info("PostMotorReadVModCompact: " + compact);

        if(!compact.contains("n_comp_chunk_center") || !compact.contains("o_comp_chunk_center"))return;
        BlockPos comp_center_old = BlockPos.of(compact.getLong("o_comp_chunk_center"));
        BlockPos comp_center_new = BlockPos.of(compact.getLong("n_comp_chunk_center"));

        BlockPos connectContext = motor.blockConnectContext();
        motor.clearCompanionShipInfo();
        motor.setBlockConnectContext(connectContext);

        ControlCraft.LOGGER.info("PostMotorReadVModCompact: {} {}", comp_center_old, comp_center_new);

        BlockPos offset = comp_center_new.subtract(comp_center_old);
        BlockPos newContact = motor.blockConnectContext().offset(offset);

        ControlCraft.LOGGER.info("PostMotorReadVModCompact: {} {}", motor.blockConnectContext(), newContact);

        ControlCraftServer.SERVER_DEFERRAL_EXECUTOR.executeLater(() -> motor.bruteDirectionalConnectWith(newContact, Direction.UP, motor.getCompanionShipAlign()), 12);

    }


    public static BlockPos centerPosOf(int x, int z){
        return new BlockPos(((x / 16 / 256 - 1) * 256 + 128) * 16, 0, ((z / 16 / 256) * 256 + 128) * 16);
    }

}
