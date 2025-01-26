package com.verr1.vscontrolcraft.compat.valkyrienskies.slider;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.function.Supplier;

public record LogicalSlider(
        long ownShipID,
        long cmpShipID,
        ServerLevel level,
        Direction slideDir,
        Vector3dc localPos_Own,
        Vector3dc localPos_Cmp,
        Supplier<Double> forceCallBack
) {
}
