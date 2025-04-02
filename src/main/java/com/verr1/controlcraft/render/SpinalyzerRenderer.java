package com.verr1.controlcraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.verr1.controlcraft.content.blocks.spinalyzer.SpinalyzerBlockEntity;
import com.verr1.controlcraft.registry.ControlCraftPartialModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class SpinalyzerRenderer extends SafeBlockEntityRenderer<SpinalyzerBlockEntity> {
    public SpinalyzerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(SpinalyzerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer Buffer = CachedBufferer.partialFacing(ControlCraftPartialModels.SPINALYZR_AXES, be.getBlockState(), Direction.SOUTH);
        Buffer.renderInto(ms, solid);
    }
}

