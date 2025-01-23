package com.verr1.vscontrolcraft.base.Schedules;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.compat.valkyrienskies.generic.QueueForceInducer;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.Queue;

public class AttractiveConnectionSchedule extends ShipQPNavigationSchedule{
    private final BlockPos xPos;
    private final BlockPos yPos;
    private final ServerLevel level;
    private final Direction xDir;
    private final Direction yDir;

    private Runnable onExpiredTask = () -> {};

    private Vector3d p_err = new Vector3d();
    private Vector3d p_int = new Vector3d();
    private double MAX_INTEGRAL = 10;

    private double p = 1;
    private double i = 1;

    @Override
    public void onExpire() {
        onExpiredTask.run();
    }

    public Vector3d getXFacePos_wc(){
        return VSMathUtils.getFaceCenterPos(level, xPos, xDir);
    }

    public Vector3d getYFacePos_wc(){
        return VSMathUtils.getFaceCenterPos(level, yPos, yDir);
    }

    public Vector3d getXFacePos_sc(){
        return VSMathUtils.getFaceCenterPosNoTransform(xPos, xDir);
    }

    public Vector3d getYFacePos_sc(){
        return VSMathUtils.getFaceCenterPosNoTransform(yPos, yDir);
    }

    public Vector3d calcAttraction(){
        return new Vector3d().fma(p, p_err).fma(i, p_int).mul(mass);
    }



    public AttractiveConnectionSchedule(
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

        init(new LevelPos(yPos, level), new Quaterniond(), new Vector3d(), timeBeforeExpired);
        //make qpSchedule only apply resistance force to make attraction progress smooth and stable
        setPID(0, 0, 14, 0);
    }



    @Override
    public void run() {
        super.run();
        ServerShip ship = VSMathUtils.getServerShip(shipPos.pos(), shipPos.level());
        if(ship == null)return;
        QueueForceInducer qfi = QueueForceInducer.getOrCreate(ship);

        p_err = getXFacePos_wc().sub(getYFacePos_wc());
        p_int = VSMathUtils.clamp(p_int.fma(ts, p_err), MAX_INTEGRAL);

        Vector3d controlForce = calcAttraction();
        Vector3d forceAtYPos = getYFacePos_sc();

        qfi.applyInvariantForceToPos(controlForce, forceAtYPos);
    }
}
