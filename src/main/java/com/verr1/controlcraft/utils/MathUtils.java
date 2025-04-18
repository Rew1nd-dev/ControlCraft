package com.verr1.controlcraft.utils;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;

import java.util.ArrayList;
import java.util.List;

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

    public static double clampHalf(double x, double max){
        return Math.min(max, Math.max(0, x));
    }

    public static int max(Integer... ints){
        int max = Integer.MIN_VALUE;
        for(int i : ints){
            max = Math.max(max, i);
        }
        return max;
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


    public static AABBd coverOf(List<Vector3dc> points){
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for(Vector3dc point : points){
            minX = Math.min(minX, point.x());
            minY = Math.min(minY, point.y());
            minZ = Math.min(minZ, point.z());
            maxX = Math.max(maxX, point.x());
            maxY = Math.max(maxY, point.y());
            maxZ = Math.max(maxZ, point.z());
        }
        return new AABBd(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static double clampDigit(double value, int digits){
        return Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
    }

    public static AABBd centerWithRadius(Vector3dc center, double r){
        return new AABBd(center.x() - r, center.y() - r, center.z() - r, center.x() + r, center.y() + r, center.z() + r);
    }

    public static ArrayList<Vector3dc> pointOf(AABBdc aabBdc){
        return new ArrayList<>(List.of(
                new Vector3d(aabBdc.minX(), aabBdc.minY(), aabBdc.minZ()),
                new Vector3d(aabBdc.maxX(), aabBdc.minY(), aabBdc.minZ()),
                new Vector3d(aabBdc.minX(), aabBdc.maxY(), aabBdc.minZ()),
                new Vector3d(aabBdc.maxX(), aabBdc.maxY(), aabBdc.minZ()),
                new Vector3d(aabBdc.minX(), aabBdc.minY(), aabBdc.maxZ()),
                new Vector3d(aabBdc.maxX(), aabBdc.minY(), aabBdc.maxZ()),
                new Vector3d(aabBdc.minX(), aabBdc.maxY(), aabBdc.maxZ()),
                new Vector3d(aabBdc.maxX(), aabBdc.maxY(), aabBdc.maxZ())
        ));
    }

}
