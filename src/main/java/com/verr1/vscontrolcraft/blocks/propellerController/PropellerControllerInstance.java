package com.verr1.vscontrolcraft.blocks.propellerController;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Supplier;


public class PropellerControllerInstance extends SingleRotatingInstance<PropellerControllerBlockEntity> {

    protected RotatingData additionalShaftHalf;

    public PropellerControllerInstance(MaterialManager materialManager, PropellerControllerBlockEntity blockEntity) {
        super(materialManager, blockEntity);

        float speed = blockEntity.getSpeed();
        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
        BlockPos pos = blockEntity.getBlockPos();
        float offset = BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos);
        Direction dir = blockState.getValue(BlockStateProperties.FACING);
        Instancer<RotatingData> half = getRotatingMaterial().getModel(AllPartialModels.SHAFT_HALF, blockState, dir);

        additionalShaftHalf = setup(half.createInstance(), speed);
        additionalShaftHalf.setRotationOffset(offset);
    }

    private PoseStack rotateToAxis(Direction.Axis axis) {
        Direction facing = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        PoseStack poseStack = new PoseStack();
        TransformStack.cast(poseStack)
                .centre()
                .rotateToFace(facing)
                .multiply(Axis.XN.rotationDegrees(-90))
                .unCentre();
        return poseStack;
    }

    @Override
    protected Instancer<RotatingData> getModel() {
        Direction facing = blockState.getValue(PropellerControllerBlock.FACING);

        return getRotatingMaterial().getModel(AllPartialModels.SHAFTLESS_COGWHEEL, blockState, facing, rotateToFace(facing));
    }

    @Override
    public void update() {
        super.update();
        if (additionalShaftHalf != null) {
            //updateRotation(additionalShaftHalf);
            additionalShaftHalf.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos));
        }
    }

    @Override
    public void updateLight() {
        super.updateLight();
        if (additionalShaftHalf != null)
            relight(pos, additionalShaftHalf);
    }

    @Override
    public void remove() {
        super.remove();
        if (additionalShaftHalf != null)
            additionalShaftHalf.delete();
    }

    private Supplier<PoseStack> rotateToFace(Direction facing) {
        return () -> {
            PoseStack stack = new PoseStack();
            TransformStack stacker = TransformStack.cast(stack)
                    .centre();

            if (facing.getAxis() == Direction.Axis.X) stacker.rotateZ(90);
            else if (facing.getAxis() == Direction.Axis.Z) stacker.rotateX(90);

            stacker.unCentre();
            return stack;
        };
    }



}
