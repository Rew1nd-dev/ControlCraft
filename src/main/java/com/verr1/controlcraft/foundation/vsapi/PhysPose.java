package com.verr1.controlcraft.foundation.vsapi;

import org.joml.Quaterniondc;
import org.joml.Vector3dc;

public interface PhysPose {

    Vector3dc getPos();

    Quaterniondc getRot();
}
