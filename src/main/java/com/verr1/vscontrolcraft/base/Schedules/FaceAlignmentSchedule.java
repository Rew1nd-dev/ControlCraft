package com.verr1.vscontrolcraft.base.Schedules;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.shadow.F;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class FaceAlignmentSchedule extends ShipQPNavigationSchedule {

    private final BlockPos xPos;
    private final BlockPos yPos;
    private final ServerLevel level;
    private final Direction xDir;
    private final Direction yDir;

    private boolean hasForcedQuaternionProvider = false;
    private boolean hasOverriddenAlignExtra = false;

    private Runnable onExpiredTask = () -> {};

    private Quaterniond extraRotation = new Quaterniond();

    private Quaterniond forcedQuaternion = new Quaterniond();

    private Quaterniond overriddenAlignExtra = new Quaterniond();

    @Override
    public void onExpire() {
        if(!alignmentDone())return;
        onExpiredTask.run();
    }

    public boolean alignmentDone(){
        Vector3d xp = VSMathUtils.getFaceCenterPos(level, xPos, xDir);
        Vector3d yp = VSMathUtils.getFaceCenterPos(level, yPos, yDir);
        return xp.sub(yp).lengthSquared() < 0.5;
    }

    public Vector3dc getXFacePos(){
        ServerShip xShip = VSGameUtilsKt.getShipObjectManagingPos(level, xPos);
        if(xShip == null)return Util.Vec3toVector3d(xPos.relative(xDir).getCenter());
        Vector3dc xFace_sc = Util.Vec3toVector3d(xPos.relative(xDir).getCenter());
        Vector3dc xFace_wc = xShip.getTransform().getShipToWorld().transformPosition(xFace_sc, new Vector3d());
        return xFace_wc;
    }

    public Quaterniondc getXBaseQuaternion(){
        ServerShip xShip = VSGameUtilsKt.getShipObjectManagingPos(level, xPos);
        if(xShip == null)return new Quaterniond();
        Quaterniondc xBaseQuaternion = xShip.getTransform().getShipToWorldRotation();
        return xBaseQuaternion;
    }

    public Quaterniondc getYTargetQuaternion(){
        if(hasForcedQuaternionProvider)return forcedQuaternion;

        Quaterniondc xBase = getXBaseQuaternion();
        Quaterniondc alignExtra = VSMathUtils.rotationToAlign(xDir, yDir);
        if(hasOverriddenAlignExtra)alignExtra = new Quaterniond();
        return xBase.mul(alignExtra, new Quaterniond()).mul(extraRotation, new Quaterniond()); //
    }

    public Vector3dc getYTargetPosition(){
        ServerShip yShip = VSGameUtilsKt.getShipObjectManagingPos(level, yPos);
        if(yShip == null)return new Vector3d(0, 0, 0);
        Vector3dc dir = Util.Vec3itoVector3d(yDir.getNormal()).mul(0.2);
        Vector3dc yFace_sc = Util.Vec3itoVector3d(yPos).add(dir);
        Vector3dc yCenter_sc = yShip.getInertiaData().getCenterOfMassInShip();
        Vector3dc relative_r_sc = new Vector3d(yFace_sc).sub(yCenter_sc, new Vector3d());

        Quaterniondc targetQuaternion = getYTargetQuaternion();
        Vector3dc relative_r_wc = targetQuaternion.transform(relative_r_sc, new Vector3d());
        Vector3dc xFace_wc = getXFacePos();
        Vector3dc yCenter_target = new Vector3d(xFace_wc).sub(relative_r_wc, new Vector3d());
        return yCenter_target;
    }


    private FaceAlignmentSchedule(
            BlockPos xPos,
            Direction xDir,
            BlockPos yPos,
            Direction yDir,
            ServerLevel level,
            int timeBeforeExpired,
            Quaterniond extraRotation,
            boolean hasForcedQuaternionProvider,
            boolean hasOverriddenAlignExtra,
            Quaterniond forcedQuaternion,
            Runnable onExpiredTask
    ){


        super(new LevelPos(yPos, level), new Quaterniond(), new Vector3d(), timeBeforeExpired);
        this.xPos = xPos;
        this.yPos = yPos;

        this.xDir = xDir;

        this.yDir = yDir;
        this.level = level;

        this.onExpiredTask = onExpiredTask;

        this.extraRotation = extraRotation;
        this.hasForcedQuaternionProvider = hasForcedQuaternionProvider;
        this.hasOverriddenAlignExtra = hasOverriddenAlignExtra;
        this.forcedQuaternion = forcedQuaternion;
    }

    public FaceAlignmentSchedule setTarget(){
        q_tar = getYTargetQuaternion();
        p_tar = getYTargetPosition();
        return this;
    }

    public static class builder{
        private BlockPos xPos;
        private BlockPos yPos;
        private ServerLevel level;
        private Direction xDir;
        private Direction yDir;

        private int timeBeforeExpired = 10;

        private boolean hasForcedQuaternionProvider = false;
        private boolean hasOverriddenAlignExtra = false;

        private Runnable onExpiredTask = () -> {};

        private Quaterniond extraRotation = new Quaterniond();

        private Quaterniond forcedQuaternion = new Quaterniond();

        public builder basic(
                 BlockPos xPos,
                 Direction xDir,
                 BlockPos yPos,
                 Direction yDir,
                 ServerLevel level,
                 int timeBeforeExpired
        ){
            this.xPos = xPos;
            this.xDir = xDir;
            this.yDir = yDir;
            this.yPos = yPos;
            this.level = level;
            this.timeBeforeExpired = timeBeforeExpired;
            return this;
        }

        public builder withExpiredTask(Runnable task){
            onExpiredTask = task;
            return this;
        }

        public builder withExtraQuaternion(Quaterniond extra){
            extraRotation = extra;
            return this;
        }

        public builder withForcedQuaternion(Quaterniond forced){
            hasForcedQuaternionProvider = true;
            forcedQuaternion = forced;
            return this;
        }

        public builder withOverriddenAlignExtra(Quaterniond extra){
            extraRotation = extra;
            hasOverriddenAlignExtra = true;
            return this;
        }

        public FaceAlignmentSchedule build(){
            return new FaceAlignmentSchedule(
                    xPos,
                    xDir,
                    yPos,
                    yDir,
                    level,
                    timeBeforeExpired,
                    extraRotation,
                    hasForcedQuaternionProvider,
                    hasOverriddenAlignExtra,
                    forcedQuaternion,
                    onExpiredTask
            ).setTarget();
        }
    }

}
