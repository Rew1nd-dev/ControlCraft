package com.verr1.vscontrolcraft.compat.valkyrienskies.slider;

import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.valkyrienskies.generic.PhysShipWrapper;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import kotlin.jvm.functions.Function1;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.lang.Math;
import java.util.concurrent.ConcurrentHashMap;

public class SliderForceInducer implements ShipForcesInducer {
    private final int lazyTickRate = 10;
    private int lazyTickCount = lazyTickRate;
    private int TICKS_BEFORE_EXPIRED = 3;
    private final ConcurrentHashMap<BlockPos, LogicalSlider> sliderProperties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BlockPos, Integer> sliderLife = new ConcurrentHashMap<>();

    public static SliderForceInducer getOrCreate(@NotNull ServerShip ship){
        SliderForceInducer obj = ship.getAttachment(SliderForceInducer.class);
        if(obj == null){
            obj = new SliderForceInducer();
            ship.saveAttachment(SliderForceInducer.class, obj);
        }
        return obj;
    }

    public void slideControlAndWritePhysics(BlockPos servoPos, PhysShipWrapper ownShip, PhysShipWrapper cmpShip, LogicalSlider property){
        BlockEntity be = property.level().getExistingBlockEntity(servoPos);
        if(!(be instanceof SliderControllerBlockEntity slider))return;
        Vector3dc own_local_pos = property.localPos_Own();
        Vector3dc cmp_local_pos = property.localPos_Cmp();

        ShipPhysics own_sp = VSMathUtils.getShipPhysics(ownShip.getImpl());
        ShipPhysics cmp_sp = VSMathUtils.getShipPhysics(cmpShip.getImpl());

        Matrix4dc own_s2w = own_sp.s2wTransform();
        Matrix4dc own_w2s = own_sp.w2sTransform();
        Matrix4dc cmp_s2w = cmp_sp.s2wTransform();

        Vector3dc own_wc = own_s2w.transformPosition(own_local_pos, new Vector3d());
        Vector3dc cmp_wc = cmp_s2w.transformPosition(cmp_local_pos, new Vector3d());
        Vector3dc sub_sc = own_w2s
                .transformDirection(
                        cmp_wc.sub(own_wc, new Vector3d()), new Vector3d()
                );

        Direction dir = property.slideDir();
        double sign = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
        double distance = switch (dir.getAxis()){
            case X -> sign * sub_sc.x();
            case Y -> sign * sub_sc.y();
            case Z -> sign * sub_sc.z();
        };

        double extra = property.forceCallBack().get();
        double mass = cmp_sp.mass();
        double scale = slider.getControllerInfoHolder().calculateControlValueScaleLinear();
        double clampedScale = VSMathUtils.clamp(scale, 1000);
        slider.getControllerInfoHolder().overrideError(distance);
        Vector3dc dirJOML = Util.Vec3itoVector3d(property.slideDir().getNormal());
        Vector3dc dirJOML_wc = own_s2w.transformDirection(new Vector3d(dirJOML));
        double cos = dirJOML_wc.angleCos(new Vector3d(0, 1, 0));
        Vector3dc controlForce_sc = dirJOML.mul((clampedScale + cos * 10) * mass + extra, new Vector3d());
        Vector3dc controlForce_wc = own_s2w.transformDirection(controlForce_sc, new Vector3d());

        Vector3dc own_r = new Vector3d(own_local_pos).sub(ownShip.getImpl().getTransform().getPositionInShip());
        Vector3dc cmp_r = new Vector3d(cmp_local_pos).sub(cmpShip.getImpl().getTransform().getPositionInShip());

        cmpShip.getImpl().applyInvariantForceToPos(controlForce_wc.mul( 1, new Vector3d()), cmp_r);
        ownShip.getImpl().applyInvariantForceToPos(controlForce_wc.mul(-1, new Vector3d()), own_r);


        slider.ownPhysics.write(own_sp);
        slider.cmpPhysics.write(cmp_sp);
        //assemShip.getImpl().applyInvariantTorque(controlTorque_wc);
        //servoShip.getImpl().applyInvariantTorque(controlTorque_wc.mul(-1, new Vector3d()));
    }


    public void updateLogicalSlider(BlockPos pos, LogicalSlider property){
        sliderProperties.put(pos, property);
        sliderLife.put(pos, TICKS_BEFORE_EXPIRED);
    }

    public void tickActivated(){
        sliderLife.entrySet().forEach(e -> e.setValue(e.getValue() - 1));
        sliderProperties.entrySet().removeIf(e -> sliderLife.get(e.getKey()) != null && sliderLife.get(e.getKey()) < 0);
        sliderLife.entrySet().removeIf(e -> e.getValue() < 0);
    }

    public void lazyTick(){
        tickActivated();
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickCount--;
        if(lazyTickCount <= 0){
            lazyTickCount = lazyTickRate;
            lazyTick();
        }
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        ShipForcesInducer.super.applyForcesAndLookupPhysShips(physShip, lookupPhysShip);
        PhysShipWrapper assemShip = new PhysShipWrapper((PhysShipImpl) physShip);
        sliderProperties.forEach(
                (blockPos, logicalSlider) -> {
                    PhysShipWrapper servoShip = new PhysShipWrapper((PhysShipImpl) lookupPhysShip.invoke(logicalSlider.ownShipID()));
                    if(servoShip.getImpl() == null)return;
                    slideControlAndWritePhysics(blockPos, servoShip, assemShip, logicalSlider);
                }
        );
    }
}
