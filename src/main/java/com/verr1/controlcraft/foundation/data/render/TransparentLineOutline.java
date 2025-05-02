package com.verr1.controlcraft.foundation.data.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.outliner.LineOutline;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public class TransparentLineOutline extends LineOutline {


    @Override
    public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt) {
        float width = params.getLineWidth();
        if (width == 0)
            return;

        VertexConsumer consumer = buffer.getBuffer(RenderTypes.getOutlineTranslucent(AllSpecialTextures.BLANK.getLocation(), true));
        params.loadColor(colorTemp);
        Vector4f color = colorTemp;
        int lightmap = LightTexture.FULL_BRIGHT;
        boolean disableLineNormals = false;
        renderInner(ms, consumer, camera, pt, width, color, lightmap, disableLineNormals);
    }

}
