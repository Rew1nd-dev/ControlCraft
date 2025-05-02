package com.verr1.controlcraft.content.cctweaked.peripheral;


import com.verr1.controlcraft.content.blocks.flap.FlapBearingBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

public class FlapBearingPeripheral extends AbstractAttachedPeripheral<FlapBearingBlockEntity> {

    public FlapBearingPeripheral(FlapBearingBlockEntity flapBearingBlockEntity) {
        super(flapBearingBlockEntity);
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof FlapBearingPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @Override
    public String getType() {
        return "WingController";
    }

    @LuaFunction
    public final double getAngle(){
        return getTarget().getAngle();
    }

    @LuaFunction
    public final void setAngle(double angle){
        getTarget().setAngle((float)angle);
    }

}
