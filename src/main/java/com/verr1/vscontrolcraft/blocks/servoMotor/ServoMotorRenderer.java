package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.verr1.vscontrolcraft.registry.AllPartialModels;
import com.verr1.vscontrolcraft.render.CachedBufferer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ServoMotorRenderer extends SafeBlockEntityRenderer<ServoMotorBlockEntity> {
    public ServoMotorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(ServoMotorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        float angle = be.getAnimatedAngle(partialTicks);
        BlockState state = be.getBlockState();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer buffer = CachedBufferer.partialFacing(AllPartialModels.SERVO_TOP, state);

        buffer.rotateCentered(state.getValue(BlockStateProperties.FACING), angle)
                .light(light)
                .renderInto(ms, solid);
    }
}
