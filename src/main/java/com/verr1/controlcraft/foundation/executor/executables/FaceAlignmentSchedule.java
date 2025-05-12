package com.verr1.controlcraft.foundation.executor.executables;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.utils.MinecraftUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class FaceAlignmentSchedule extends ShipQPNavigationSchedule {

    private boolean force_connect = false;

    private final BlockPos xPos;
    private final BlockPos yPos;

    private final Direction xAlign;
    private final Direction yAlign;

    private final Direction xForward;
    private final Direction yForward;

    private final ServerLevel level;

    private Runnable onExpiredTask = () -> {};

    @Override
    public void onExpired() {
        if(!alignmentDone())return;
        onExpiredTask.run();
    }

    public boolean alignmentDone(){
        if(force_connect)return true;
        Vector3d xp = VSMathUtils.getFaceCenterPos(level, xPos, xAlign);
        Vector3d yp = VSMathUtils.getFaceCenterPos(level, yPos, yAlign);
        return xp.sub(yp).lengthSquared() < 4;
    }

    public Vector3dc getXFacePos(){
        ServerShip xShip = VSGameUtilsKt.getShipObjectManagingPos(level, xPos);
        if(xShip == null)return ValkyrienSkies.set(new Vector3d(), xPos.relative(xAlign).getCenter());
        Vector3dc xFace_sc = ValkyrienSkies.set(new Vector3d(), xPos.relative(xAlign).getCenter());
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
        Quaterniondc alignExtra = VSMathUtils.rotationToAlign(xAlign, xForward, yAlign, yForward);
        return xBase.mul(alignExtra, new Quaterniond()); //
    }

    public Vector3dc getYTargetPosition(){
        ServerShip yShip = VSGameUtilsKt.getShipObjectManagingPos(level, yPos);
        if(yShip == null)return new Vector3d(0, 0, 0);
        Vector3dc dir = ValkyrienSkies.set(new Vector3d(), yAlign.getNormal()).mul(0.2);
        Vector3dc yFace_sc = ValkyrienSkies.set(new Vector3d(), yPos).add(dir);
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
            Direction xAlign,
            Direction xForward,
            BlockPos yPos,
            Direction yAlign,
            Direction yForward,
            ServerLevel level,
            boolean forced,
            int timeBeforeExpired,
            Runnable onExpiredTask
    ){


        super(WorldBlockPos.of(level, yPos), new Quaterniond(), new Vector3d(), timeBeforeExpired);
        this.xPos = xPos;
        this.yPos = yPos;

        this.force_connect = forced;

        this.xAlign = xAlign;
        this.yAlign = yAlign;

        this.xForward = xForward;
        this.yForward = yForward;

        this.level = level;

        this.onExpiredTask = onExpiredTask;
    }

    public FaceAlignmentSchedule setTarget(){
        q_tar = getYTargetQuaternion();
        p_tar = getYTargetPosition();
        return this;
    }

    public static class builder{

        private final BlockPos xPos;
        private final BlockPos yPos;

        private final Direction xAlign;
        private final Direction yAlign;

        private Direction xForward;
        private Direction yForward;

        private final ServerLevel level;
        private int timeBeforeExpired = 10;

        private boolean force_connect = false;

        private Runnable onExpiredTask = () -> {};

        public builder(BlockPos xPos, Direction xAlign, BlockPos yPos, Direction yAlign, ServerLevel level){
            this.xPos = xPos;
            this.yPos = yPos;
            this.xAlign = xAlign;
            this.yAlign = yAlign;
            this.level = level;

            this.xForward = MinecraftUtils.getVerticalDirectionSimple(xAlign);
            this.yForward = MinecraftUtils.getVerticalDirectionSimple(yAlign);

        }

        public builder withOnExpiredTask(Runnable task){
            onExpiredTask = task;
            return this;
        }

        public builder withGivenXForward(Direction xForward){
            this.xForward = xForward;
            return this;
        }

        public builder withGivenYForward(Direction yForward){
            this.yForward = yForward;
            return this;
        }

        public builder withTimeBeforeExpired(int timeBeforeExpired){
            this.timeBeforeExpired = timeBeforeExpired;
            return this;
        }

        public FaceAlignmentSchedule build(){
            return new FaceAlignmentSchedule(
                    xPos,
                    xAlign,
                    xForward,
                    yPos,
                    yAlign,
                    yForward,
                    level,
                    force_connect,
                    timeBeforeExpired,
                    onExpiredTask
            ).setTarget();
        }

        public builder withForced(){
            force_connect = true;
            return this;
        }

    }

}
