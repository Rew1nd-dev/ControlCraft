package com.verr1.vscontrolcraft.blocks.slider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.verr1.vscontrolcraft.registry.AllPartialModels;
import com.verr1.vscontrolcraft.render.CachedBufferer;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.*;

public class SliderRenderer extends SafeBlockEntityRenderer<SliderControllerBlockEntity> {
    public SliderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(SliderControllerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        float distance = (float) VSMathUtils.clamp0(be.getAnimatedTargetDistance(partialTicks), 32) ;
        BlockState state = be.getBlockState();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer buffer_top = CachedBufferer.partialFacing(AllPartialModels.SLIDER_TOP, state);
        SuperByteBuffer buffer_pil = CachedBufferer.partial(AllPartialModels.SLIDER_PILLAR, state);


        Vector3fc scale = new Vector3f(1, 1, 1 + distance);//be.getDirectionJOML().absolute().mul(distance).add(ones).get(new Vector3f()) ;

        // Apply scaling transformation

        buffer_pil
                .centre()
                .rotateToFace(
                        be.getDirection().getOpposite()
                )
                .unCentre()
                .scale(1, 1, 1 + distance)
                .light(light)
                .renderInto(ms, solid);


        buffer_top.translate(new Vector3f(be.getDirectionJOML().get(new Vector3f()).mul(distance)))
                .light(light)
                .renderInto(ms, solid);
    }
}
