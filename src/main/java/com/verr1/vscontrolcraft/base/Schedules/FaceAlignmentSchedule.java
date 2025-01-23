package com.verr1.vscontrolcraft.base.Schedules;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class FaceAlignmentSchedule extends ShipQPNavigationSchedule {

    private final BlockPos xPos;
    private final BlockPos yPos;
    private final ServerLevel level;
    private final Direction xDir;
    private final Direction yDir;

    private Runnable onExpiredTask = () -> {};

    @Override
    public void onExpire() {
        if(!alignmentDone())return;
        onExpiredTask.run();
    }

    public boolean alignmentDone(){
        Vector3d xp = VSMathUtils.getAbsolutePosition(xPos, level, xDir);
        Vector3d yp = VSMathUtils.getAbsolutePosition(yPos, level, yDir);
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
        Quaterniondc xBase = getXBaseQuaternion();
        Quaterniondc alignExtra = VSMathUtils.rotationToAlign(xDir, yDir);
        return xBase.mul(alignExtra, new Quaterniond());
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


    public FaceAlignmentSchedule(
            BlockPos xPos,
            Direction xDir,
            BlockPos yPos,
            Direction yDir,
            ServerLevel level,
            int timeBeforeExpired,
            Runnable onExpiredTask
    ){

        this.xPos = xPos;
        this.yPos = yPos;

        this.xDir = xDir;

        this.yDir = yDir;
        this.level = level;

        this.onExpiredTask = onExpiredTask;

        q_tar = getYTargetQuaternion();
        p_tar = getYTargetPosition();
        init(new LevelPos(yPos, level), q_tar, p_tar, timeBeforeExpired);
    }

}
