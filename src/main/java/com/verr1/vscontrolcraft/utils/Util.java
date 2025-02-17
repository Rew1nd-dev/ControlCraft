package com.verr1.vscontrolcraft.utils;

import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.utility.Pair;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;
import java.util.List;

import static com.verr1.vscontrolcraft.registry.AllBlockStates.ROTATION;

public class Util {

    public static Vec3 vec3add(Vec3 a, Vec3 b){
        return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vector3d Vec3toVector3d(Vec3 vec3){
        return new Vector3d(vec3.x, vec3.y, vec3.z);
    }

    public static Vector3d Vec3itoVector3d(Vec3i vec3){
        return new Vector3d(vec3.getX(), vec3.getY(), vec3.getZ());
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

    public double getDouble(List<Double> list, int index){
        if(index < list.size()){
            return list.get(index);
        }
        return 0;
    }

    public static Direction getVerticalDirection(BlockState state){
        if(!state.hasProperty(BlockStateProperties.FACING) ||
                !state.hasProperty(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))return Direction.UP;

        Direction facing = state.getValue(BlockStateProperties.FACING);
        Boolean align = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

    public static double clamp1(double x){
        return Math.atan(x) / Math.PI * 0.5;
    }

    public static Matrix3d getRotationMatrix(BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        int rotation = state.getValue(ROTATION);

        Matrix3d matrix = switch (facing) {
            case UP -> getRotationMatrixForAxis(Direction.Axis.Y, 0);
            case DOWN -> getRotationMatrixForAxis(Direction.Axis.Y, 180);
            case NORTH -> getRotationMatrixForAxis(Direction.Axis.Z, 0);
            case SOUTH -> getRotationMatrixForAxis(Direction.Axis.Z, 180);
            case EAST -> getRotationMatrixForAxis(Direction.Axis.X, 90);
            case WEST -> getRotationMatrixForAxis(Direction.Axis.X, -90);
        };


        Matrix3d additionalRotation = getRotationMatrixForAxis(Direction.Axis.Y, rotation * 90);
        matrix.mul(additionalRotation);

        return matrix;
    }

    private static Matrix3d getRotationMatrixForAxis(Direction.Axis axis, int degrees) {
        float radians = (float) Math.toRadians(degrees);
        Matrix3d matrix = new Matrix3d();

        switch (axis) {
            case X:
                matrix.m11 = (float) Math.cos(radians);
                matrix.m12 = (float) -Math.sin(radians);
                matrix.m21 = (float) Math.sin(radians);
                matrix.m22 = (float) Math.cos(radians);
                break;
            case Y:
                matrix.m00 = (float) Math.cos(radians);
                matrix.m02 = (float) Math.sin(radians);
                matrix.m20 = (float) -Math.sin(radians);
                matrix.m22 = (float) Math.cos(radians);
                break;
            case Z:
                matrix.m00 = (float) Math.cos(radians);
                matrix.m01 = (float) -Math.sin(radians);
                matrix.m10 = (float) Math.sin(radians);
                matrix.m11 = (float) Math.cos(radians);
                break;
        }

        return matrix;
    }



    public static boolean tryParseLongFilter(String s) {
        if(s.isEmpty() || s.equals("-"))return true;
        try{
            Long.parseLong(s);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static long tryParseLong(String s) {
        try{
            return Long.parseLong(s);
        }catch (NumberFormatException e){
            return 0;
        }
    }

    public static boolean tryParseDoubleFilter(String s) {
        if(s.isEmpty() || s.equals("-"))return true;
        try{
            Double.parseDouble(s);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static boolean tryParseClampedDoubleFilter(String s, double threshold){
        if(s.isEmpty() || s.equals("-"))return true;
        try{
            double d = Double.parseDouble(s);
            if(Math.abs(d) < threshold)return true;
        }catch (NumberFormatException e){
            return false;
        }
        return false;
    }

    public static double tryParseDouble(String s) {
        try{
            return Double.parseDouble(s);
        }catch (NumberFormatException e){
            return 0;
        }
    }




}
