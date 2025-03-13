package com.verr1.controlcraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.verr1.controlcraft.content.blocks.motor.RevoluteMotorBlockEntity;
import com.verr1.controlcraft.registry.ControlCraftPartialModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RevoluteMotorRenderer extends SafeBlockEntityRenderer<RevoluteMotorBlockEntity> {
    public RevoluteMotorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(RevoluteMotorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        float angle = -be.getAnimatedAngle(partialTicks);
        BlockState state = be.getBlockState();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer buffer = CachedBufferer.partialFacing(ControlCraftPartialModels.SERVO_TOP, state);

        buffer.rotateCentered(state.getValue(BlockStateProperties.FACING), (float) Math.toRadians(angle))
                .light(light)
                .renderInto(ms, solid);
    }
}
