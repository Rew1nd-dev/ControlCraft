package com.verr1.controlcraft.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.content.blocks.jet.JetRudderBlockEntity;
import com.verr1.controlcraft.registry.ControlCraftPartialModels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.joml.Vector2dc;

public class JetRudderRenderer extends SafeBlockEntityRenderer<JetRudderBlockEntity> {
    public JetRudderRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    protected void renderSafe(JetRudderBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        float base_offset = (float) Math.toRadians(0);

        Couple<Double> angles = be.getRenderAngles();

        float horizontal = angles.get(true).floatValue();
        float vertical = angles.get(false).floatValue();
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        VertexConsumer translucent = bufferSource.getBuffer(RenderType.translucent());

        SuperByteBuffer rudder =
                CachedBufferer
                        .partial(ControlCraftPartialModels.RUDDER_PART, be.getBlockState());


        rudder
                .centre()
                .rotateToFace(be.getDirection())
                .unCentre()
                .rotateCentered(Direction.NORTH, 0)
                .translate(-0.5f, 0, -0.5f)
                .rotateCentered(Direction.UP, -horizontal + base_offset)

                .light(light)
                .renderInto(ms, solid);

        rudder
                .centre()
                .rotateToFace(be.getDirection())
                .unCentre()
                .rotateCentered(Direction.NORTH, 3.14f)
                .translate(-0.5f, 0, -0.5f)
                .rotateCentered(Direction.UP, horizontal + base_offset)

                .light(light)
                .renderInto(ms, solid);

        rudder
                .centre()
                .rotateToFace(be.getDirection())
                .unCentre()
                .rotateCentered(Direction.NORTH, -1.57f)
                .translate(-0.5f, 0, -0.5f)
                .rotateCentered(Direction.UP, vertical + base_offset)

                .light(light)
                .renderInto(ms, solid);

        rudder
                .centre()
                .rotateToFace(be.getDirection())
                .unCentre()
                .rotateCentered(Direction.NORTH, 1.57f)
                .translate(-0.5f, 0, -0.5f)
                .rotateCentered(Direction.UP, -vertical + base_offset)

                .light(light)
                .renderInto(ms, solid);

    }
}
