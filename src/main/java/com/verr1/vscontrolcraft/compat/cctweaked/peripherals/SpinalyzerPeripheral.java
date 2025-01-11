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

public class SpinalyzerPeripheral implements IPeripheral {
    private SpinalyzerBlockEntity spinalyzerBlockEntity;

    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    public SpinalyzerPeripheral(SpinalyzerBlockEntity be){
        spinalyzerBlockEntity = be;
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public Object getTarget(){
        return spinalyzerBlockEntity;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public String getType() {
        return "spinalyzer";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (iPeripheral instanceof PropellerControllerPeripheral)return false;
        if (iPeripheral == null)return false;
        return spinalyzerBlockEntity == iPeripheral.getTarget();
    }

    @LuaFunction
    public List<Double> getQuaternion(){
        Quaterniondc q = spinalyzerBlockEntity.getQuaternion();
        computers.forEach(e->{e.queueEvent("t", 1);});
        return List.of(q.x(), q.y(), q.z(), q.w());
    }

    @LuaFunction
    public List<List<Double>> getTransform(){
        Matrix3d m = spinalyzerBlockEntity.getRotationMatrix_wc2sc();
        return List.of(
            List.of(m.m00, m.m10, m.m20),
            List.of(m.m01, m.m11, m.m21),
            List.of(m.m02, m.m12, m.m22)
        );
    }

    @LuaFunction
    public List<List<Double>> getRelativeTransform(){
        Matrix3d m = spinalyzerBlockEntity.getRelativeSourceTransform();
        return List.of(
            List.of(m.m00, m.m01, m.m02),
            List.of(m.m10, m.m11, m.m12),
            List.of(m.m20, m.m21, m.m22)
        );
    }

    @LuaFunction
    public double getRelativeAngle(int axis){
        return spinalyzerBlockEntity.getRotationAngle(axis);
    }

    @LuaFunction
    public Map<String, Object> getPhysicsInfo(){
        return spinalyzerBlockEntity.readPhysicsShipInfo().getCCPhysics();
    }





}
