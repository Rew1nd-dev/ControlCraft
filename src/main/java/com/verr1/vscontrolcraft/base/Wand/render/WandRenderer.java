package com.verr1.vscontrolcraft.base.Wand.render;

import com.simibubi.create.CreateClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WandRenderer {

    public static void drawOutline(BlockPos selection, int color, String slot) {
        Level world = Minecraft.getInstance().level;
        if (selection == null)
            return;

        BlockPos pos = selection;
        BlockState state = world.getBlockState(pos);
        VoxelShape shape = state.getShape(world, pos);
        AABB boundingBox = shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds();
        CreateClient.OUTLINER.showAABB(slot, boundingBox.move(pos))
                .colored(color)
                .lineWidth(1 / 16f);
    }
}
