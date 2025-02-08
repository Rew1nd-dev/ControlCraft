package com.verr1.vscontrolcraft.blocks.magnet;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.compat.valkyrienskies.magnet.LogicalMagnet;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MagnetManager {
    private static final int TICKS_BEFORE_EXPIRED = 10;


    private static final Map<LogicalMagnet, Integer> magnets = new ConcurrentHashMap<>();

    public static void activate(LogicalMagnet magnet){
        magnets.put(magnet, TICKS_BEFORE_EXPIRED);
    }

    public static void tickActivated(){
        magnets.entrySet().forEach(e -> e.setValue(e.getValue() - 1));
        magnets.entrySet().removeIf(e -> e.getValue() < 0);
    }

    private static boolean isValid(LogicalMagnet element){
        if(element.level().getExistingBlockEntity(element.pos()) instanceof MagnetBlockEntity magnet){
            return !magnet.isRemoved();
        }
        return false;
    }

    public static @Nullable MagnetBlockEntity getExisting(LevelPos element){
        if(element.level().getExistingBlockEntity(element.pos()) instanceof MagnetBlockEntity magnet){
            if(magnet.isRemoved())return null;
            return magnet;
        }
        return null;
    }

    public static @Nullable MagnetBlockEntity getExisting(LogicalMagnet element){
        if(element.level().getExistingBlockEntity(element.pos()) instanceof MagnetBlockEntity magnet){
            if(magnet.isRemoved())return null;
            return magnet;
        }
        return null;
    }

    private static @Nullable MagnetBlockEntity getExistingUnchecked(LogicalMagnet element){
        return (MagnetBlockEntity) element.level().getExistingBlockEntity(element.pos());
    }

    // calc i ->> j (i is attracted)
    public static Vector3d calculateAttraction(LogicalMagnet i, LogicalMagnet j){
        MagnetBlockEntity e_i = getExisting(i);
        MagnetBlockEntity e_j = getExisting(j);
        if(e_i == null || e_j == null)return new Vector3d();
        if(!e_i.isOnServerShip())return new Vector3d();
        if(e_j.isOnServerShip() &&
                Objects.requireNonNull(e_j.getServerShipOn()).getId() ==
                Objects.requireNonNull(e_i.getServerShipOn()).getId())
            return new Vector3d();
        if(!e_j.isOnServerShip() && VSGameUtilsKt.isBlockInShipyard(j.level(), j.pos()))return new Vector3d();
        if(i.equals(j))return new Vector3d();

        Vector3dc p_i = e_i.getPosition_wc();
        Vector3dc p_j = e_j.getPosition_wc();
        Vector3d v_ij = new Vector3d(p_j).sub(p_i, new Vector3d());
        //if(v_ij.lengthSquared() < 1e-4)return new Vector3d();

        return VSMathUtils.ColumnFunction(e_i.getStrength(), e_j.getStrength(), v_ij);
    }

    public static Vector3d calculateAttraction(LogicalMagnet magnet){
        Vector3d sumAttraction = new Vector3d();
        magnets.forEach((key, value) -> sumAttraction.add(calculateAttraction(magnet, key)));
        return sumAttraction;
    }

    public static void tick(){
        tickActivated();
    }
}
