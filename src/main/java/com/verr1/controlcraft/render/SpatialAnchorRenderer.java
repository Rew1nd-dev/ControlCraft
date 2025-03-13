package com.verr1.controlcraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlockEntity;
import com.verr1.controlcraft.registry.ControlCraftPartialModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SpatialAnchorRenderer extends SafeBlockEntityRenderer<SpatialAnchorBlockEntity> {


    public SpatialAnchorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(SpatialAnchorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        boolean isRunning = be.isRunning();
        boolean isStatic = be.isStatic();
        BlockState state = be.getBlockState();
        VertexConsumer translucent = bufferSource.getBuffer(RenderType.cutout());
        SuperByteBuffer buffer = CachedBufferer.partialFacing(ControlCraftPartialModels.SPATIAL_CORE, state);

        float speed = isStatic ? 10f : 120f;
        float on = isRunning ? 1f : 0f;

        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float angle = ((time * speed * on * 3f / 10) % 360) / 180 * (float) Math.PI;

        buffer.rotateCentered(state.getValue(BlockStateProperties.FACING), angle)
                .light(light)
                .renderInto(ms, translucent);


    }
}
