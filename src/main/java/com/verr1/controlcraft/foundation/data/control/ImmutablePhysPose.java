package com.verr1.controlcraft.foundation.data.control;

import com.verr1.controlcraft.foundation.vsapi.PhysPose;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public record ImmutablePhysPose(Vector3dc pos, Quaterniondc rot) implements PhysPose {

    public static ImmutablePhysPose EMPTY = new ImmutablePhysPose(new Vector3d(), new Quaterniond());

    public static ImmutablePhysPose of(Vector3dc pos, Quaterniondc rot){
        return new ImmutablePhysPose(new Vector3d(pos), new Quaterniond(rot));
    }

    @NotNull
    @Override
    public Vector3dc getPos() {
        return pos;
    }

    @NotNull
    @Override
    public Quaterniondc getRot() {
        return rot;
    }
}
