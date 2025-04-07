package com.verr1.controlcraft.content.cctweaked.peripheral;

import com.verr1.controlcraft.content.blocks.spinalyzer.SpinalyzerBlockEntity;
import com.verr1.controlcraft.foundation.data.ShipPhysics;
import com.verr1.controlcraft.utils.CCUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;

import java.util.List;
import java.util.Map;

public class SpinalyzerPeripheral extends AbstractAttachedPeripheral<SpinalyzerBlockEntity> {

    public SpinalyzerPeripheral(SpinalyzerBlockEntity be){
        super(be);
    }


    @Override
    public String getType() {
        return "spinalyzer";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof SpinalyzerPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public final Map<String, Double> getQuaternion(){
        Quaterniondc q = getTarget().getQuaternion();
        return CCUtils.dumpVec4(q);
    }

    @LuaFunction
    public final Map<String, Double> getQuaternionJ(){
        Quaterniondc q = getTarget().getQuaternion().conjugate(new Quaterniond());
        return CCUtils.dumpVec4(q);
    }

    @LuaFunction
    public final List<List<Double>> getRotationMatrix(){
        Matrix3dc m = getTarget().getRotationMatrix_w2s();
        return CCUtils.dumpMat3(m);
    }

    @LuaFunction
    public final List<List<Double>> getRotationMatrixT(){
        Matrix3dc m = getTarget().getRotationMatrix_s2w();
        return CCUtils.dumpMat3(m);
    }

    @LuaFunction
    public final  Map<String, Double> getVelocity(){
        return CCUtils.dumpVec3(getTarget().getVelocity());
    }

    @LuaFunction
    public final  Map<String, Double> getAngularVelocity(){
        return CCUtils.dumpVec3(getTarget().getAngularVelocity());
    }

    @LuaFunction
    public final Map<String, Double> getPosition(){
        return CCUtils.dumpVec3(getTarget().getPosition());
    }

    @LuaFunction
    public final Map<String, Double> getSpinalyzerPosition(){
        return CCUtils.dumpVec3(getTarget().getSpinalyzerPosition());
    }

    @LuaFunction
    public final Map<String, Double> getSpinalyzerVelocity(){
        return CCUtils.dumpVec3(getTarget().getSpinalyzerVelocity());
    }

    @LuaFunction
    public final Map<String, Object> getPhysics(){
        return getTarget().readSelf().getCCPhysics();
    }


    public void queueEvent(ShipPhysics sp){
        getComputers().forEach((c) -> c.queueEvent("phys_tick", sp.getCCPhysics()));
    }



}
