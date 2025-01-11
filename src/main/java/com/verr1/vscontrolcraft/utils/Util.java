package com.verr1.vscontrolcraft.utils;

import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;
import org.joml.Matrix3f;
import org.joml.Vector3d;

import java.util.function.Predicate;

import static com.verr1.vscontrolcraft.registry.AllBlockStates.ROTATION;

public class Util {
    ;

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

    public static Pair<Integer, Integer> getRotations(Direction facing, int rotation) {
           // 获取旋转

        int xRotation = 0; // 默认 XRotation
        int yRotation = switch (facing) {
            case UP -> {
                xRotation = -90;
                yield rotation * 90;
            }
            case DOWN -> {
                xRotation = 90;
                yield rotation * 90;
            }
            case NORTH -> {
                xRotation = 0;
                yield rotation * 90;
            }
            case SOUTH -> {
                xRotation = 0;
                yield 180 + rotation * 90;
            }
            case EAST -> {
                xRotation = 0;
                yield 90 + rotation * 90;
            }
            case WEST -> {
                xRotation = 0;
                yield 270 + rotation * 90;
            }
        };

        yRotation = Math.floorMod(yRotation, 360);

        return Pair.of(xRotation, yRotation);
    }

    public static boolean tryParseLongFilter(String s) {
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
        try{
            Double.parseDouble(s);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static double tryParseDouble(String s) {
        try{
            return Double.parseDouble(s);
        }catch (NumberFormatException e){
            return 0;
        }
    }
}
