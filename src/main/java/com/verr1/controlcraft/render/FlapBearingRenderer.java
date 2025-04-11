package com.verr1.controlcraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.verr1.controlcraft.content.blocks.flap.FlapBearingBlockEntity;
import com.verr1.controlcraft.registry.ControlCraftPartialModels;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FlapBearingRenderer extends SafeBlockEntityRenderer<FlapBearingBlockEntity> {
    public FlapBearingRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(FlapBearingBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        float angle = be.getClientAnimatedAngle().getValue(partialTicks);
        Direction dir = be.getDirection();
        int sign = (dir == Direction.UP || dir == Direction.SOUTH || dir == Direction.EAST) ? 1 : -1;
        BlockState state = be.getBlockState();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer propellerBuffer = CachedBufferer.partialFacing(ControlCraftPartialModels.WING_CONTROLLER_TOP, state);

        propellerBuffer.rotateCentered(state.getValue(BlockStateProperties.FACING), (float) Math.toRadians(angle * sign))
                .light(light)
                .renderInto(ms, solid);
    }
}
