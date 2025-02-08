package com.verr1.vscontrolcraft.blocks.wingController;

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
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class WingControllerRenderer extends SafeBlockEntityRenderer<WingControllerBlockEntity> {
    public WingControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(WingControllerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        float angle = be.clientAnimatedAngle.getValue();
        Direction dir = be.getDirection();
        int sign = (dir == Direction.UP || dir == Direction.SOUTH || dir == Direction.EAST) ? 1 : -1;
        BlockState state = be.getBlockState();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer propellerBuffer = CachedBufferer.partialFacing(AllPartialModels.WING_CONTROLLER_TOP, state);

        propellerBuffer.rotateCentered(state.getValue(BlockStateProperties.FACING), (float) Math.toRadians(angle * sign))
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(ms, solid);
    }
}
