package com.verr1.controlcraft.content.compact.vmod;

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
        CompoundTag tag = new CompoundTag();
        motor.writeCompact(motorOriginal);
        Ship comp = motor.getCompanionServerShip();
        if(comp == null || motor.blockConnectContext().equals(BlockPos.ZERO))return null;
        int xM = motor.blockConnectContext().getX();
        int zM = motor.blockConnectContext().getZ();

        BlockPos center_xz_old = centerPosOf(xM, zM);
        compact.putLong("o_comp_chunk_center", center_xz_old.asLong());

        tag.put("motor", motorOriginal);
        tag.put("compact", compact);
        return tag;
    }

    public static void PreMotorReadVModCompact(
            @NotNull ServerLevel serverLevel,
            @NotNull Map<Long, Long> map,
            @Nullable CompoundTag tagToModify
    ){
        if(tagToModify == null)return;
        CompoundTag motorOriginal = tagToModify.getCompound("motor");
        long o_comp_id = motorOriginal.getLong("companionShipID");
        if(!map.containsKey(o_comp_id))return;
        long n_comp_id = map.get(o_comp_id);
        Ship n_comp = ValkyrienSkies.getShipWorld(serverLevel).getAllShips().getById(n_comp_id);
        if(n_comp == null)return;
        int xM = n_comp.getChunkClaim().getXMiddle() * 16;
        int zM = n_comp.getChunkClaim().getZMiddle() * 16;

        BlockPos center_xz_new = centerPosOf(xM, zM);

        tagToModify.getCompound("compact").putLong("n_comp_chunk_center", center_xz_new.asLong());
    }


    public static void PostMotorReadVModCompact(AbstractMotor motor, CompoundTag tag){
        CompoundTag compact = tag.getCompound("compact");
        if(!compact.contains("n_comp_chunk_center") || !compact.contains("o_comp_chunk_center"))return;
        BlockPos comp_center_old = BlockPos.of(compact.getLong("o_comp_chunk_center"));
        BlockPos comp_center_new = BlockPos.of(compact.getLong("n_comp_chunk_center"));

        BlockPos offset = comp_center_new.subtract(comp_center_old);
        BlockPos newContact = motor.blockConnectContext().offset(offset);
        motor.bruteDirectionalConnectWith(newContact, Direction.UP, motor.getCompanionShipAlign());
    }


    public static BlockPos centerPosOf(int x, int z){
        return new BlockPos(((x / 16 / 256 - 1) * 256 + 128) * 16, 0, ((z / 16 / 256) * 256 + 128) * 16);
    }

}
