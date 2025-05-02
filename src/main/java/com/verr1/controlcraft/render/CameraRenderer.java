package com.verr1.controlcraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.outliner.LineOutline;
import com.simibubi.create.foundation.outliner.Outline;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.Color;
import com.verr1.controlcraft.content.blocks.camera.CameraBlockEntity;
import com.verr1.controlcraft.foundation.data.render.Line;
import com.verr1.controlcraft.foundation.data.render.RayLerpHelper;
import com.verr1.controlcraft.foundation.managers.ClientCameraManager;
import com.verr1.controlcraft.registry.ControlCraftPartialModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.awt.*;

import static com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies.toJOML;

public class CameraRenderer extends SafeBlockEntityRenderer<CameraBlockEntity> {
    public CameraRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    protected void renderSafe(
            CameraBlockEntity be,
            float partialTicks,
            PoseStack ms,
            MultiBufferSource bufferSource,
            int light,
            int overlay
    ) {
        CameraBlockEntity linkedCamera = ClientCameraManager.getLinkedCamera();
        if(linkedCamera != null && linkedCamera.getBlockPos().equals(be.getBlockPos()))return;

        BlockState state = be.getBlockState();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.translucent());
        SuperByteBuffer lensBuffer = CachedBufferer.partialFacing(ControlCraftPartialModels.CAMERA_LENS, state);


        lensBuffer
                .light(light)
                .renderInto(ms, solid);
    }
}
