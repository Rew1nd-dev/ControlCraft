package com.verr1.vscontrolcraft.blocks.jetRudder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.verr1.vscontrolcraft.registry.AllPartialModels;
import com.verr1.vscontrolcraft.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class JetRudderRenderer extends SafeBlockEntityRenderer<JetRudderBlockEntity> {
    public JetRudderRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    protected void renderSafe(JetRudderBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        float base_offset = (float) Math.toRadians(0);
        float horizontal = (float) Math.toRadians(be.animatedHorizontalAngle.getValue(partialTicks)) ;
        float vertical = (float) Math.toRadians(be.animatedVerticalAngle.getValue(partialTicks));
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer rudder = CachedBufferer.partial(AllPartialModels.RUDDER_PART, be.getBlockState());

        rudder
            .centre()
            .rotateToFace(be.getDirection())
            .unCentre()

            .rotateCentered(Direction.NORTH, 0)
            .translate(-0.3f, 0, 0)
            .rotateCentered(Direction.UP, -horizontal + base_offset)

            .light(light)
            .renderInto(ms, solid);

        rudder
            .centre()
            .rotateToFace(be.getDirection())
            .unCentre()

            .rotateCentered(Direction.NORTH, 3.14f)
            .translate(-0.3f, 0, 0)
            .rotateCentered(Direction.UP, horizontal + base_offset)

            .light(light)
            .renderInto(ms, solid);

        rudder
            .centre()
            .rotateToFace(be.getDirection())
            .unCentre()

            .rotateCentered(Direction.NORTH, -1.57f)
            .translate(-0.3f, 0, 0)
            .rotateCentered(Direction.UP, -vertical + base_offset)

            .light(light)
            .renderInto(ms, solid);

        rudder
            .centre()
            .rotateToFace(be.getDirection())
            .unCentre()
            .rotateCentered(Direction.NORTH, 1.57f)
            .translate(-0.3f, 0, 0)
            .rotateCentered(Direction.UP, vertical + base_offset)

            .light(light)
            .renderInto(ms, solid);

    }
}
