package com.verr1.vscontrolcraft.utils;

import com.verr1.vscontrolcraft.ControlCraft;
import net.minecraft.nbt.CompoundTag;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.apigame.constraints.*;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;

import java.util.Arrays;

public class VSConstrainSerializeUtils {

    // I am fed up with these, only convert the constraint I need

    public static void writeVSAttachmentConstrain(CompoundTag tag, String identifier, VSAttachmentConstraint constrain){
        tag.putString("identifier", identifier);
        tag.putLong(identifier + "_ship_1", constrain.component1());
        tag.putLong(identifier + "_ship_2", constrain.component2());
        tag.putDouble(identifier + "_compliance", constrain.component3());
        writeVector3d(tag, identifier + "_p_1", constrain.component4());
        writeVector3d(tag, identifier + "_p_2", constrain.component5());
        tag.putDouble(identifier + "_mf", constrain.component6());
        tag.putDouble(identifier + "_scale", constrain.component7());
    }

    public static VSAttachmentConstraint readVSAttachmentConstrain(CompoundTag tag, String identifier){
        try{
            if(!tag.getString("identifier").equals(identifier))return null;
            long s1 = tag.getLong(identifier + "_ship_1");
            long s2 = tag.getLong(identifier + "_ship_2");
            double comp = tag.getDouble(identifier + "_compliance");
            Vector3dc p1 = readVector3d(tag, identifier + "_p_1");
            Vector3dc p2 = readVector3d(tag, identifier + "_p_2");
            double mf = tag.getDouble(identifier + "_mf");
            double scale = tag.getDouble(identifier + "_scale");

            return new VSAttachmentConstraint(s1, s2, comp, p1, p2, mf, scale);
            
        }catch (Exception e){
            ControlCraft.LOGGER.info("readVSAttachmentConstrain() failed\n" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static void writeVSHingeOrientationConstrain(CompoundTag tag, String identifier, VSHingeOrientationConstraint constrain){
        tag.putString("identifier", identifier);
        tag.putLong(identifier + "_ship_1", constrain.component1());
        tag.putLong(identifier + "_ship_2", constrain.component2());
        tag.putDouble(identifier + "_compliance", constrain.component3());
        writeQuaternion(tag, identifier + "_q_1", constrain.component4());
        writeQuaternion(tag, identifier + "_q_2", constrain.component5());
        tag.putDouble(identifier + "_mf", constrain.component6());

    }

    public static VSHingeOrientationConstraint readVSHingeOrientationConstrain(CompoundTag tag, String identifier){
        try{
            if(!tag.getString("identifier").equals(identifier))return null;
            long s1 = tag.getLong(identifier + "_ship_1");
            long s2 = tag.getLong(identifier + "_ship_2");
            double comp = tag.getDouble(identifier + "_compliance");
            Quaterniondc q1 = readQuaternion(tag, identifier + "_q_1");
            Quaterniondc q2 = readQuaternion(tag, identifier + "_q_2");
            double mf = tag.getDouble(identifier + "_mf");

            return new VSHingeOrientationConstraint(s1, s2, comp, q1, q2, mf);

        }catch (Exception e){
            ControlCraft.LOGGER.info("readVSHingeOrientationConstrain() failed\n" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static void writeVSSlideConstrain(CompoundTag tag, String identifier, VSSlideConstraint constraint){
        tag.putString("identifier", identifier);
        tag.putLong(identifier + "_ship_1", constraint.component1());
        tag.putLong(identifier + "_ship_2", constraint.component2());
        tag.putDouble(identifier + "_compliance", constraint.component3());
        writeVector3d(tag, identifier + "_p_1", constraint.component4());
        writeVector3d(tag, identifier + "_p_2", constraint.component5());
        tag.putDouble(identifier + "_mf", constraint.component6());
        writeVector3d(tag, identifier + "_axis", constraint.component7());
        tag.putDouble(identifier + "_scale", constraint.component8());
    }

    public static VSSlideConstraint readVSSlideConstrain(CompoundTag tag, String identifier){
        try{
            if(!tag.getString("identifier").equals(identifier))return null;
            long s1 = tag.getLong(identifier + "_ship_1");
            long s2 = tag.getLong(identifier + "_ship_2");
            double comp = tag.getDouble(identifier + "_compliance");
            Vector3dc p1 = readVector3d(tag, identifier + "_p_1");
            Vector3dc p2 = readVector3d(tag, identifier + "_p_2");
            double mf = tag.getDouble(identifier + "_mf");
            Vector3dc axis = readVector3d(tag, identifier + "_axis");
            double scale = tag.getDouble(identifier + "_scale");

            return new VSSlideConstraint(s1, s2, comp, p1, p2, mf, axis, scale);

        }catch (Exception e){
            ControlCraft.LOGGER.info("readVSSlideConstrain() failed\n" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static void writeVSFixedOrientationConstraint(CompoundTag tag, String identifier, VSFixedOrientationConstraint constraint){
        tag.putString("identifier", identifier);
        tag.putLong(identifier + "_ship_1", constraint.component1());
        tag.putLong(identifier + "_ship_2", constraint.component2());
        tag.putDouble(identifier + "_compliance", constraint.component3());
        writeQuaternion(tag, identifier + "_q_1", constraint.component4());
        writeQuaternion(tag, identifier + "_q_2", constraint.component5());
        tag.putDouble(identifier + "_mt", constraint.component6());
    }

    public static VSFixedOrientationConstraint readVSFixedOrientationConstraint(CompoundTag tag, String identifier){
        try{
            if(!tag.getString("identifier").equals(identifier))return null;
            long s1 = tag.getLong(identifier + "_ship_1");
            long s2 = tag.getLong(identifier + "_ship_2");
            double comp = tag.getDouble(identifier + "_compliance");
            Quaterniondc q1 = readQuaternion(tag, identifier + "_q_1");
            Quaterniondc q2 = readQuaternion(tag, identifier + "_q_2");
            double mf = tag.getDouble(identifier + "_mt");

            return new VSFixedOrientationConstraint(s1, s2, comp, q1, q2, mf);

        }catch (Exception e){
            ControlCraft.LOGGER.info("readVSFixedOrientationConstraint() failed\n" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static void writeVSRopeConstraint(CompoundTag tag, String identifier, VSRopeConstraint constraint){
        tag.putString("identifier", identifier);
        tag.putLong(identifier + "_ship_1", constraint.component1());
        tag.putLong(identifier + "_ship_2", constraint.component2());
        tag.putDouble(identifier + "_compliance", constraint.component3());
        writeVector3d(tag, identifier + "_p_1", constraint.component4());
        writeVector3d(tag, identifier + "_p_2", constraint.component5());
        tag.putDouble(identifier + "_mf", constraint.component6());
        tag.putDouble(identifier + "_length", constraint.component7());
    }

    public static VSRopeConstraint readVSRopeConstraint(CompoundTag tag, String identifier){
        try{
            if(!tag.getString("identifier").equals(identifier))return null;
            long s1 = tag.getLong(identifier + "_ship_1");
            long s2 = tag.getLong(identifier + "_ship_2");
            double comp = tag.getDouble(identifier + "_compliance");
            Vector3dc p1 = readVector3d(tag, identifier + "_p_1");
            Vector3dc p2 = readVector3d(tag, identifier + "_p_2");
            double mf = tag.getDouble(identifier + "_mf");
            double length = tag.getDouble(identifier + "_length");

            return new VSRopeConstraint(s1, s2, comp, p1, p2, mf, length);

        }catch (Exception e){
            ControlCraft.LOGGER.info("readVSRopeConstraint() failed\n" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static void writeVSPosDampingConstraint(CompoundTag tag, String identifier, VSPosDampingConstraint constraint){
        tag.putString("identifier", identifier);
        tag.putLong(identifier + "_ship_1", constraint.component1());
        tag.putLong(identifier + "_ship_2", constraint.component2());
        tag.putDouble(identifier + "_compliance", constraint.component3());
        writeVector3d(tag, identifier + "_p_1", constraint.component4());
        writeVector3d(tag, identifier + "_p_2", constraint.component5());
        tag.putDouble(identifier + "_mf", constraint.component6());
        tag.putDouble(identifier + "_pD", constraint.component7());
    }

    public static VSPosDampingConstraint readVSPosDampingConstraint(CompoundTag tag, String identifier){
        try{
            if(!tag.getString("identifier").equals(identifier))return null;
            long s1 = tag.getLong(identifier + "_ship_1");
            long s2 = tag.getLong(identifier + "_ship_2");
            double comp = tag.getDouble(identifier + "_compliance");
            Vector3dc p1 = readVector3d(tag, identifier + "_p_1");
            Vector3dc p2 = readVector3d(tag, identifier + "_p_2");
            double mf = tag.getDouble(identifier + "_mf");
            double pD = tag.getDouble(identifier + "_pD");

            return new VSPosDampingConstraint(s1, s2, comp, p1, p2, mf, pD);

        }catch (Exception e){
            ControlCraft.LOGGER.info("readVSPosDampingConstraint() failed\n" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }




    public static void writeVector3d(CompoundTag tag, String identifier, Vector3dc vec){
        tag.putDouble(identifier + "_x", vec.x());
        tag.putDouble(identifier + "_y", vec.y());
        tag.putDouble(identifier + "_z", vec.z());
    }

    public static Vector3d readVector3d(CompoundTag tag, String identifier){
        double x = tag.getDouble(identifier + "_x");
        double y = tag.getDouble(identifier + "_y");
        double z = tag.getDouble(identifier + "_z");
        return new Vector3d(x, y, z);
    }

    public static void writeQuaternion(CompoundTag tag, String identifier, Quaterniondc q){
        tag.putDouble(identifier + "_x", q.x());
        tag.putDouble(identifier + "_y", q.y());
        tag.putDouble(identifier + "_z", q.z());
        tag.putDouble(identifier + "_w", q.w());
    }

    public static Quaterniond readQuaternion(CompoundTag tag, String identifier){
        double x = tag.getDouble(identifier + "_x");
        double y = tag.getDouble(identifier + "_y");
        double z = tag.getDouble(identifier + "_z");
        double w = tag.getDouble(identifier + "_w");
        return new Quaterniond(x, y, z, w);
    }


    // I am fed up with this, only to convert the constraint I need
    public static VSConstraint convertGroundId(VSConstraint constraint, ServerShipWorldCore sswc, String dimensionID, Boolean convert_ship_0){
        Long groundID = sswc.getDimensionToGroundBodyIdImmutable().get(dimensionID);
        long ship_ID_0 =  convert_ship_0 ? groundID : constraint.getShipId0();
        long ship_ID_1 = !convert_ship_0 ? groundID : constraint.getShipId1();
        switch (constraint.getConstraintType()){
            case HINGE_ORIENTATION -> {
                VSHingeOrientationConstraint c = (VSHingeOrientationConstraint) constraint;
                return new VSHingeOrientationConstraint(
                        ship_ID_0,
                        ship_ID_1,
                        c.component3(),
                        c.component4(),
                        c.component5(),
                        c.component6()
                );
            }
            case ATTACHMENT, FIXED_ATTACHMENT_ORIENTATION -> {
                VSAttachmentConstraint c = (VSAttachmentConstraint) constraint;
                return new VSAttachmentConstraint(
                        ship_ID_0,
                        ship_ID_1,
                        c.component3(),
                        c.component4(),
                        c.component5(),
                        c.component6(),
                        c.component7()
                );
            }
            case FIXED_ORIENTATION -> {
                VSFixedOrientationConstraint c = (VSFixedOrientationConstraint) constraint;
                return new VSFixedOrientationConstraint(
                        ship_ID_0,
                        ship_ID_1,
                        c.component3(),
                        c.component4(),
                        c.component5(),
                        c.component6()
                );
            }
            case ROPE -> {
                VSRopeConstraint c = (VSRopeConstraint) constraint;
                return new VSRopeConstraint(
                        ship_ID_0,
                        ship_ID_1,
                        c.component3(),
                        c.component4(),
                        c.component5(),
                        c.component6(),
                        c.component7()
                );
            }
            case SLIDE -> {
                VSSlideConstraint c = (VSSlideConstraint) constraint;
                return new VSSlideConstraint(
                        ship_ID_0,
                        ship_ID_1,
                        c.component3(),
                        c.component4(),
                        c.component5(),
                        c.component6(),
                        c.component7(),
                        c.component8()
                );
            }
            case POS_DAMPING -> {
                VSPosDampingConstraint c = (VSPosDampingConstraint) constraint;
                return new VSPosDampingConstraint(
                        ship_ID_0,
                        ship_ID_1,
                        c.component3(),
                        c.component4(),
                        c.component5(),
                        c.component6(),
                        c.component7()
                );
            }
            default -> {
                return constraint;
            }
        }
    }

}
