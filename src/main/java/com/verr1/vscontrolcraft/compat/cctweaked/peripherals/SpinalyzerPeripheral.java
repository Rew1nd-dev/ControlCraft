package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlockEntity;
import com.verr1.vscontrolcraft.utils.CCUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public List<Double> getQuaternion(){
        Quaterniondc q = getTarget().getQuaternion();
        return List.of(q.x(), q.y(), q.z(), q.w());
    }

    @LuaFunction
    public List<List<Double>> getTransform(){
        Matrix3d m = getTarget().getRotationMatrix_wc2sc();
        return List.of(
            List.of(m.m00, m.m10, m.m20),
            List.of(m.m01, m.m11, m.m21),
            List.of(m.m02, m.m12, m.m22)
        );
    }

    @LuaFunction
    public List<List<Double>> getRelativeTransform(){
        Matrix3d m = getTarget().getRelativeSourceTransform();
        return List.of(
            List.of(m.m00, m.m01, m.m02),
            List.of(m.m10, m.m11, m.m12),
            List.of(m.m20, m.m21, m.m22)
        );
    }

    @LuaFunction
    public double getRelativeAngle(int axis){
        return getTarget().getRotationAngle(axis);
    }

    @LuaFunction
    public Map<String, Object> getPhysicsInfo(){
        return getTarget().physics.read().getCCPhysics();
    }





}
