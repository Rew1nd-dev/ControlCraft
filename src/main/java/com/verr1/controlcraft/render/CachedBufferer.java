package com.verr1.controlcraft.render;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.BakedModelRenderHelper;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.verr1.controlcraft.ControlCraftClient;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class CachedBufferer {
    public static final SuperByteBufferCache.Compartment<BlockState> CC_GENERIC_BLOCK = new SuperByteBufferCache.Compartment<>();
    public static final SuperByteBufferCache.Compartment<PartialModel> CC_PARTIAL = new SuperByteBufferCache.Compartment<>();
    public static final SuperByteBufferCache.Compartment<Pair<Direction, PartialModel>> CC_DIRECTIONAL_PARTIAL = new SuperByteBufferCache.Compartment<>();

    public static SuperByteBuffer block(BlockState toRender) {
        return block(CC_GENERIC_BLOCK, toRender);
    }

    public static SuperByteBuffer block(SuperByteBufferCache.Compartment<BlockState> compartment, BlockState toRender) {
        return ControlCraftClient.BUFFER_CACHE.get(compartment, toRender, () -> BakedModelRenderHelper.standardBlockRender(toRender));
    }

    public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState) {
        return ControlCraftClient.BUFFER_CACHE.get(CC_PARTIAL, partial,
                () -> BakedModelRenderHelper.standardModelRender(partial.get(), referenceState));
    }

    public static SuperByteBuffer partial(PartialModel partial, BlockState referenceState,
                                          Supplier<PoseStack> modelTransform) {
        return ControlCraftClient.BUFFER_CACHE.get(CC_PARTIAL, partial,
                () -> BakedModelRenderHelper.standardModelRender(partial.get(), referenceState, modelTransform.get()));
    }

    public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState) {
        Direction facing = referenceState.getValue(FACING);
        return partialFacing(partial, referenceState, facing);
    }

    public static SuperByteBuffer partialFacing(PartialModel partial, BlockState referenceState, Direction facing) {
        return partialDirectional(partial, referenceState, facing,
                rotateToFace(facing));
    }

    public static SuperByteBuffer partialFacingVertical(PartialModel partial, BlockState referenceState, Direction facing) {
        return partialDirectional(partial, referenceState, facing,
                rotateToFaceVertical(facing));
    }

    public static SuperByteBuffer partialDirectional(PartialModel partial, BlockState referenceState, Direction dir,
                                                     Supplier<PoseStack> modelTransform) {
        return ControlCraftClient.BUFFER_CACHE.get(CC_DIRECTIONAL_PARTIAL, Pair.of(dir, partial),
                () -> BakedModelRenderHelper.standardModelRender(partial.get(), referenceState, modelTransform.get()));
    }

    public static Supplier<PoseStack> rotateToFace(Direction facing) {
        return () -> {
            PoseStack stack = new PoseStack();
            TransformStack.cast(stack)
                    .centre()
                    .rotateY(AngleHelper.horizontalAngle(facing))
                    .rotateX(AngleHelper.verticalAngle(facing))
                    .unCentre();
            return stack;
        };
    }

    public static Supplier<PoseStack> rotateToFaceVertical(Direction facing) {
        return () -> {
            PoseStack stack = new PoseStack();
            TransformStack.cast(stack)
                    .centre()
                    .rotateY(AngleHelper.horizontalAngle(facing))
                    .rotateX(AngleHelper.verticalAngle(facing) + 90)
                    .unCentre();
            return stack;
        };
    }
}
