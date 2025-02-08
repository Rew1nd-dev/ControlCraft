package com.verr1.vscontrolcraft.blocks.camera;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

public class CameraBlockEntity extends SmartBlockEntity {
    private Vector3d cameraPosition;
    private Quaterniondc cameraBaseRotation;
    private Vector3d cameraPosition_prev;
    private Quaterniondc cameraBaseRotation_prev;

    private float pitch = 0;
    private float yaw = 0;

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public CameraBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    public Vector3d getCameraPosition() {
        Vec3 front = getBlockPos().getCenter();
        Vector3d front3d_wc = new Vector3d(front.x, front.y, front.z);
        if(!level.isClientSide)return front3d_wc;
        ClientShip ship = (ClientShip)VSGameUtilsKt.getShipManagingPos(level, getBlockPos());
        if(ship == null)return front3d_wc;
        Vector3dc shipPos_wc = ship.getTransform().getPositionInShip();
        Vector3d relative_sc = new Vector3d(front3d_wc).sub(shipPos_wc);
        Vector3d relative_wc = ship.getTransform().getShipToWorld().transformDirection(new Vector3d(relative_sc));
        Vector3d cameraPos_wc = relative_wc.add(ship.getTransform().getPositionInWorld());
        return cameraPos_wc;
    }

    public Vector3d getCameraPositionShip(){
        Vec3 front = getBlockPos().getCenter();
        Vector3d front3d_wc = new Vector3d(front.x, front.y, front.z);
        return front3d_wc;
    }

    public ClientShip getClientShip(){
        ClientShip ship = (ClientShip)VSGameUtilsKt.getShipManagingPos(level, getBlockPos());
        return ship;
    }

    public Quaterniondc getCameraBaseRotation(){
        Quaterniond q = new Quaterniond();
        if(!level.isClientSide)return q;
        ClientShip ship = (ClientShip)VSGameUtilsKt.getShipManagingPos(level, getBlockPos());
        if(ship == null)return q;
        return ship.getTransform().getShipToWorldRotation();
    }

    @Override
    public void tick() {
        super.tick();
        if(!level.isClientSide)return;
        cameraPosition_prev = cameraPosition;
        cameraBaseRotation_prev = cameraBaseRotation;
        cameraPosition = getCameraPosition();
        cameraBaseRotation = getCameraBaseRotation();
    }

    public Vector3d getLerpCameraPosition(float partialTicks){
        if(cameraPosition_prev == null)return cameraPosition;
        Vector3d lerped = new Vector3d(cameraPosition).sub(cameraPosition_prev).mul(partialTicks).add(cameraPosition);
        return lerped;
    }

    public Quaterniondc getLerpCameraBaseRotation(float partialTicks){
        if(cameraBaseRotation_prev == null)return cameraBaseRotation;
        Quaterniond lerped = new Quaterniond(cameraBaseRotation).nlerp(cameraBaseRotation_prev, partialTicks);
        return lerped;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }
}
