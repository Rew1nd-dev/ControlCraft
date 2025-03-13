package com.verr1.controlcraft.foundation.managers;


import com.verr1.controlcraft.foundation.api.ISpatialTarget;
import com.verr1.controlcraft.foundation.data.logical.LogicalSpatial;
import com.verr1.controlcraft.utils.VSGetterUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.HashMap;

public class SpatialLinkManager {
    private static final int lazyTickRate = 5;
    private static int lazyTick = 0;

    private static final int TICKS_BEFORE_EXPIRED = 2;

    private static final HashMap<ISpatialTarget, Integer> anchors = new HashMap<>();


    // Unchecked
    public static void activate(ISpatialTarget anchor){
        anchors.remove(anchor);
        anchors.put(anchor, TICKS_BEFORE_EXPIRED);
    }

    public static void tickActivated(){
        anchors.entrySet().forEach(e -> e.setValue(e.getValue() - 1));
        anchors.entrySet().removeIf(e -> e.getValue() < 0);
    }

    public static boolean filter(ISpatialTarget x, ISpatialTarget y){
        if(x.pos().equals(y.pos()))return false;
        if(x.protocol() != y.protocol())return false;
        if(x.isStatic() && y.isStatic() || !x.isStatic() && !y.isStatic())return false;
        if(!x.dimensionID().equals(y.dimensionID()))return false;
        if(VSGetterUtils.isOnSameShip(x.levelPos(), y.levelPos()))return false;

        return true;
    }

    public static double distance(ISpatialTarget x, ISpatialTarget y){
        if(!filter(x, y))return Integer.MAX_VALUE;
        return distanceUnchecked(x, y);
    }

    public static double distanceUnchecked(ISpatialTarget x, ISpatialTarget y){
        return
                VSGetterUtils.getAbsolutePosition(x.levelPos())
                    .sub(
                VSGetterUtils.getAbsolutePosition(y.levelPos()), new Vector3d()
                    ).lengthSquared();
    }

    public static @Nullable ISpatialTarget link(LogicalSpatial anchor){
        return anchors
                .keySet()
                .stream()
                .filter(a -> filter(a, anchor))
                .min(Comparator.comparingDouble
                        (a -> distanceUnchecked(a, anchor))
                )
                .orElse(null);
    }

    public static void lazyTick(){
        if(--lazyTick > 0)return;
        lazyTick = lazyTickRate;
        tickActivated();
    }

    public static void tick(){
        lazyTick();
    }

}
