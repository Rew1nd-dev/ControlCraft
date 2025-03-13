package com.verr1.controlcraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlockEntity;
import com.verr1.controlcraft.registry.ControlCraftPartialModels;
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
        boolean spinal_only = state.getValue(PropellerBlock.HAS_BLADES);
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());

        SuperByteBuffer propellerBuffer = CachedBufferer
                .partialFacing(
                        spinal_only ?
                                ControlCraftPartialModels.NORMAL_PROPELLER :
                                ControlCraftPartialModels.NORMAL_PROPELLER_CENTER,
                        state
                );

        propellerBuffer.rotateCentered(state.getValue(BlockStateProperties.FACING), angle)
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(ms, solid);
    }
}
