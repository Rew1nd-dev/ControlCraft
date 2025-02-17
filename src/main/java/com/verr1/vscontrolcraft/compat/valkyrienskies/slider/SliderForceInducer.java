package com.verr1.vscontrolcraft.compat.valkyrienskies.slider;

import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.valkyrienskies.base.AbstractExpirableForceInducer;
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
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.lang.Math;

public class SliderForceInducer extends AbstractExpirableForceInducer {


    public static SliderForceInducer getOrCreate(@NotNull ServerShip ship){
        SliderForceInducer obj = ship.getAttachment(SliderForceInducer.class);
        if(obj == null){
            obj = new SliderForceInducer();
            ship.saveAttachment(SliderForceInducer.class, obj);
        }
        return obj;
    }
    /*
    *
    *
    * */
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


        double metric = property.isAdjustingPosition() ?
                distance :
                VSMathUtils.get_dy2x_sc(ownShip, cmpShip, property.slideDir());

        double extra = property.force();
        double mass = cmp_sp.mass();
        double scale = slider.getControllerInfoHolder().calculateControlValueScaleLinear();
        double clampedScale = VSMathUtils.clamp(scale, 1000);

        int id = cmpShip.getImpl().getTransform().getShipToWorldScaling().minComponent();
        double scale_m = cmpShip.getImpl().getTransform().getShipToWorldScaling().get(id);
        double mass_scale_ratio = Math.pow(scale_m, 3);

        slider.getControllerInfoHolder().overrideError(metric);
        Vector3dc dirJOML = Util.Vec3itoVector3d(property.slideDir().getNormal());
        Vector3dc dirJOML_wc = own_s2w.transformDirection(new Vector3d(dirJOML));
        double cos = dirJOML_wc.angleCos(new Vector3d(0, 1, 0));
        Vector3dc controlForce_sc = dirJOML.mul((clampedScale + cos * 10) * mass * mass_scale_ratio + extra, new Vector3d());
        Vector3dc controlForce_wc = own_s2w.transformDirection(controlForce_sc, new Vector3d());

        Vector3dc own_r = new Vector3d(own_local_pos).sub(ownShip.getImpl().getTransform().getPositionInShip());
        Vector3dc cmp_r = new Vector3d(cmp_local_pos).sub(cmpShip.getImpl().getTransform().getPositionInShip());

        cmpShip.getImpl().applyInvariantForceToPos(controlForce_wc.mul( 1, new Vector3d()), cmp_r);
        if(property.shouldCounter())ownShip.getImpl().applyInvariantForceToPos(controlForce_wc.mul(-1, new Vector3d()), own_r);


        slider.ownPhysics.write(own_sp);
        slider.cmpPhysics.write(cmp_sp);

    }




    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        super.applyForces(physShip);
        lazyTickLives();

    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        PhysShipWrapper assemShip = new PhysShipWrapper((PhysShipImpl) physShip);
        getLives().forEach(
            (levelPos, integer) -> {
                if(!(VSMathUtils.getExisting(levelPos) instanceof SliderControllerBlockEntity slider)) return;
                LogicalSlider logicalSlider = slider.getLogicalSlider();
                if(logicalSlider == null)return;
                PhysShipWrapper servoShip = new PhysShipWrapper((PhysShipImpl) lookupPhysShip.invoke(logicalSlider.ownShipID()));
                if(servoShip.getImpl() == null)return;
                slideControlAndWritePhysics(levelPos.pos(), servoShip, assemShip, logicalSlider);
            }
        );
    }
}
