package com.verr1.vscontrolcraft.compat.valkyrienskies.wing;

import org.joml.Matrix3d;
import org.joml.Vector3d;

/*
*   wc : world coordinates
*   sc : ship coordinates
*   lc : logicalWing coordinates
*/

public class LogicalWing {
    private Vector3d anchor_pos_wc;
    private Matrix3d transform_sc2lc;

    private double kd;
    private double I_dA;
    private double I_rdA;



}
