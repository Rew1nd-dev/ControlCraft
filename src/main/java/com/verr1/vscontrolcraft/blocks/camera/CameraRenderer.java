package com.verr1.vscontrolcraft.blocks.camera;

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

public class CameraRenderer extends SafeBlockEntityRenderer<CameraBlockEntity> {
    public CameraRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    protected void renderSafe(CameraBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        CameraBlockEntity linkedCamera = LinkedCameraManager.getLinkedCamera();
        if(linkedCamera != null && LinkedCameraManager.getLinkCameraPos().equals(be.getBlockPos()))return;

        BlockState state = be.getBlockState();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer propellerBuffer = CachedBufferer.partialFacing(AllPartialModels.CAMERA_LENS, state);

        propellerBuffer
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(ms, solid);
    }
}
