package com.verr1.controlcraft.utils;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class MathUtils {

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double max) {
        return clamp(value, -max, max);
    }

    public static double clamp1(double x){
        return Math.atan(x) / Math.PI * 0.5;
    }


    public static Vector3d abs(Vector3dc v){
        return new Vector3d(Math.abs(v.x()), Math.abs(v.y()), Math.abs(v.z()));
    }

    public static Vector3d clamp(Vector3dc value, double max) {
        double x = clamp(value.x(), max);
        double y = clamp(value.y(), max);
        double z = clamp(value.z(), max);
        return new Vector3d(x, y, z);
    }

    public static double radErrFix(double err){
        if(err > Math.PI){
            return err - 2 * Math.PI;
        }
        if(err < -Math.PI){
            return err + 2 * Math.PI;
        }
        return err;
    }

    public static float angleReset(float angle){
        while(angle > 180){
            angle -= 360;
        }
        while(angle < -180){
            angle += 360;
        }
        return angle;
    }

    public static double angleReset(double angle){
        while(angle > 180){
            angle -= 360;
        }
        while(angle < -180){
            angle += 360;
        }
        return angle;
    }

    public static double radianReset(double radian){
        while(radian > Math.PI){
            radian -= 2 * Math.PI;
        }
        while(radian < -Math.PI){
            radian += 2 * Math.PI;
        }
        return radian;
    }

}
