package com.verr1.vscontrolcraft.utils;

import com.verr1.vscontrolcraft.ControlCraft;
import net.minecraft.nbt.CompoundTag;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint;

import java.util.Arrays;

public class VSConstrainSerializeUtils {

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

}
