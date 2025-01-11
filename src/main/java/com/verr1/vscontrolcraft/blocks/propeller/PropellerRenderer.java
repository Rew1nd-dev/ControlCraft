package com.verr1.vscontrolcraft.blocks.propeller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.verr1.vscontrolcraft.registry.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.verr1.vscontrolcraft.render.CachedBufferer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PropellerRenderer extends SafeBlockEntityRenderer<PropellerBlockEntity> {
    public PropellerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(PropellerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        float angle = be.angle.getValue(partialTicks);
        BlockState state = be.getBlockState();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer propellerBuffer = CachedBufferer.partialFacing(AllPartialModels.NORMAL_PROPELLER, state);

        propellerBuffer.rotateCentered(state.getValue(BlockStateProperties.FACING), angle)
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(ms, solid);
    }
}
