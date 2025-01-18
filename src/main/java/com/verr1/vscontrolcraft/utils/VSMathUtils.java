package com.verr1.vscontrolcraft.utils;

import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import net.minecraft.core.Direction;
import org.joml.*;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.impl.game.ships.PhysInertia;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.physics_api.PoseVel;

import javax.annotation.Nullable;
import java.lang.Math;

//XC2YC:  [X, Y, Z]: X: the X unit basis in YC coordinate represented by XC coordinate unit vector basis,
//                      such that XC2YC * v_x(represented in XC coordinate) = v_y(represented in YC coordinate)

public class VSMathUtils {
    public static Quaterniondc getQuaternionOfPlacement(Direction facing){
        return switch (facing){
            case DOWN -> new Quaterniond(new AxisAngle4d(Math.PI, new Vector3d(1.0, 0.0, 0.0)));
            case NORTH -> (new Quaterniond(new AxisAngle4d(Math.PI, new Vector3d(0.0, 1.0, 0.0)))).mul((new Quaterniond(new AxisAngle4d(Math.PI / 2, (Vector3dc)(new Vector3d(1.0, 0.0, 0.0)))))).normalize();
            case EAST -> new Quaterniond(new AxisAngle4d(Math.PI / 2, (Vector3dc) new Vector3d(0.0, 1.0, 0.0))).mul(new Quaterniond(new AxisAngle4d(Math.PI / 2, (Vector3dc)(new Vector3d(1.0, 0.0, 0.0))))).normalize();
            case SOUTH -> (new Quaterniond(new AxisAngle4d(Math.PI / 2, (Vector3dc)(new Vector3d(1.0, 0.0, 0.0))))).normalize();
            case WEST ->(new Quaterniond(new AxisAngle4d(Math.PI * 3 / 2, (Vector3dc)(new Vector3d(0.0, 1.0, 0.0))))).mul((Quaterniondc)(new Quaterniond(new AxisAngle4d(Math.PI / 2, (Vector3dc)(new Vector3d(1.0, 0.0, 0.0)))))).normalize();
            default -> new Quaterniond();
        };
    }


    public static double clamp(double x, double threshold){
        if(x > threshold)return threshold;
        if(x < - threshold)return threshold;
        return x;
    }

    public static Matrix3d get_wc2sc(@Nullable Ship ship){
        if(ship == null)return new Matrix3d();
        return ship.getTransform().getWorldToShip().get3x3(new Matrix3d());
    }

    public static Matrix3d get_sc2wc(@Nullable Ship ship){
        if(ship == null)return new Matrix3d();
        return ship.getTransform().getShipToWorld().get3x3(new Matrix3d());
    }

    public static Matrix3d get_xc2yc(@Nullable Ship ship_x, @Nullable Ship ship_y){
        Matrix3d wc2sc_x = get_wc2sc(ship_x);
        Matrix3d wc2sc_y = get_wc2sc(ship_y);
        return get_xc2yc(wc2sc_x, wc2sc_y);
    }

    public static Matrix3d get_xc2yc(Matrix3dc wc2sc_x, Matrix3dc wc2sc_y){
        return new Matrix3d(wc2sc_x).mul(new Matrix3d(wc2sc_y).transpose());
    }

    public static double get_xc2yc(Matrix3dc wc2sc_x, Matrix3dc wc2sc_y, Direction direction){
        Matrix3d m = get_xc2yc(wc2sc_x, wc2sc_y);
        return get_xc2yc(m, direction);
    }

    public static double get_xc2yc(Matrix3dc xc2yc, Direction direction){
        Direction.Axis axis = direction.getAxis();
        double sign = (direction == Direction.UP || direction == Direction.WEST || direction == Direction.NORTH) ? -1 : 1;
        if(axis == Direction.Axis.X){ // rotating around x-axis
            return Math.atan2(xc2yc.m21(), xc2yc.m22()) * sign; // z.y / z.z
        }
        if (axis == Direction.Axis.Y){ // rotating around y-axis
            return Math.atan2(xc2yc.m20(), xc2yc.m22()) * sign; // z.x / z.z
        }
        if (axis == Direction.Axis.Z){ // rotating around z-axis
            return Math.atan2(xc2yc.m10(), xc2yc.m11()) * sign; // y.x / y.y
        }
        return 0;
    }

    /*
    *
        if(sign_0 == 1){
            int temp = s_axis_x1;
            s_axis_x1 = s_axis_x2;
            s_axis_x2 = temp;
        }// yc2xc
    *
    * */
    public static double get_xc2yc(Matrix3dc xc2yc, Direction xDir, Direction yDir){
        //xc2yc = xc2yc.transpose(new Matrix3d());
        int[] shuffle_1 = {2, 0, 1}; // z->y->x
        int[] shuffle_2 = {1, 2, 0}; // z->x->y
        Direction.Axis axis_x = xDir.getAxis();
        Direction.Axis axis_y = yDir.getAxis();
        int sign_0 = (xDir == Direction.DOWN || xDir == Direction.WEST || xDir == Direction.NORTH) ? -1 : 1;

        int s_axis_y0 = shuffle_1[axis_y.ordinal()];
        int s_axis_x1 = shuffle_2[axis_x.ordinal()];
        int s_axis_x2 = shuffle_1[axis_x.ordinal()];


        double Y = xc2yc.get(s_axis_y0, s_axis_x1);
        double X = xc2yc.get(s_axis_y0, s_axis_x2);
        return -sign_0 * Math.atan2(X, Y);

    }


    public static double get_xc2yc(Matrix3dc wc2sc_x, Matrix3dc wc2sc_y, Direction xDir, Direction yDir){
        //Matrix3d align_y2x = rotationToAlign(xDir, yDir).get(new Matrix3d());
        //Matrix3d wc2sc_yy = new Matrix3d(align_y2x.transpose()).mul(wc2sc_y);
        //Matrix3d m = get_xc2yc(wc2sc_x, wc2sc_yy); // y * x_t
        return get_xc2yc(get_xc2yc(wc2sc_x, wc2sc_y), xDir, yDir);
    }

    public static Matrix3d q2m(Quaterniondc q){
        return q.get(new Matrix3d());
    }

    public static double get_xc2yc(@Nullable Ship ship_x, @Nullable Ship ship_y, Direction direction){
        Matrix3d m = get_xc2yc(ship_x, ship_y);
        return get_xc2yc(m, direction);

    }

    public static double get_xc2yc(Ship ship_x, Ship ship_y, Direction xDir, Direction yDir){
        Matrix3d wc2sc_x = get_wc2sc(ship_x);
        Matrix3d wc2sc_y = get_wc2sc(ship_y);
        return get_xc2yc(wc2sc_x, wc2sc_y, xDir, yDir);
    }

    public static double radErrFix(double radErr){
        if(radErr > Math.PI){
            return radErr - 2 * Math.PI;
        }
        if(radErr < -Math.PI){
            return radErr + 2 * Math.PI;
        }
        return radErr;
    }

    public static ShipPhysics getShipPhysics(PhysShipImpl ship){
        if(ship == null)return ShipPhysics.EMPTY;
        PoseVel poseVel = ship.getPoseVel();
        PhysInertia inertia = ship.getInertia();
        return new ShipPhysics(
                new Vector3d(poseVel.getVel()),
                new Vector3d(poseVel.getOmega()),
                new Vector3d(poseVel.getPos()),
                new Quaterniond(poseVel.getRot()),
                new Matrix3d(inertia.getMomentOfInertiaTensor()),
                new Matrix3d(poseVel.getRot().get(new Matrix3d())),
                inertia.getShipMass()
        );
    }

    public static Vector3dc clamp(Vector3dc v, double threshold){
        Vector3d vc = new Vector3d(v);
        vc.x = clamp(vc.x, threshold);
        vc.y = clamp(vc.y, threshold);
        vc.z = clamp(vc.z, threshold);
        return vc;
    }

    public static Vector3dc clamp(Vector3d v, double threshold){
        v.x = clamp(v.x, threshold);
        v.y = clamp(v.y, threshold);
        v.z = clamp(v.z, threshold);
        return v;
    }

    public static Quaterniond rotationToAlign(Direction staticFace, Direction dynamicFace){
        return new Quaterniond(getQuaternionOfPlacement(staticFace.getOpposite())).mul(new Quaterniond(getQuaternionOfPlacement(dynamicFace)).conjugate()).normalize();
    }
}
