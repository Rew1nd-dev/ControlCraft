package com.verr1.vscontrolcraft.base.Wand.render;

import com.simibubi.create.CreateClient;
import com.verr1.vscontrolcraft.base.Wand.ClientWand;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(value = Dist.CLIENT)
public class WandRenderer {
    public static Vec3
            NWD = new Vec3(0, 0, 0),
            NED = new Vec3(1, 0, 0),
            SWD = new Vec3(0, 0, 1),
            SED = new Vec3(1, 0, 1),
            NWU = new Vec3(0, 1, 0),
            NEU = new Vec3(1, 1, 0),
            SWU = new Vec3(0, 1, 1),
            SEU = new Vec3(1, 1, 1);

    public record FaceVec3(Vec3 f1, Vec3 f2, Vec3 f3, Vec3 f4) {
        public FaceVec3 withRelative(BlockPos pos){
            Vec3 base = new Vec3(pos.getX(), pos.getY(), pos.getZ());
            Vec3 _f1 = Util.vec3add(base, f1);
            Vec3 _f2 = Util.vec3add(base, f2);
            Vec3 _f3 = Util.vec3add(base, f3);
            Vec3 _f4 = Util.vec3add(base, f4);
            return new FaceVec3(_f1, _f2, _f3, _f4);
        }
    }

    public static FaceVec3 getFaceVec3(BlockPos pos, Direction face){
        return switch (face) {
            case DOWN -> new FaceVec3(NWD, NED, SED, SWD).withRelative(pos);
            case UP -> new FaceVec3(NWU, NEU, SEU, SWU).withRelative(pos);
            case NORTH -> new FaceVec3(NWD, NED, NEU, NWU).withRelative(pos);
            case SOUTH -> new FaceVec3(SWD, SED, SEU, SWU).withRelative(pos);
            case WEST -> new FaceVec3(NWD, NWU, SWU, SWD).withRelative(pos);
            case EAST -> new FaceVec3(NED, SED, SEU, NEU).withRelative(pos);
        };
    }

    public static void drawOutline(BlockPos selection, int color, String slot) {
        Level world = Minecraft.getInstance().level;
        if (selection == null)
            return;
        if(world == null)return;

        BlockState state = world.getBlockState(selection);
        VoxelShape shape = state.getShape(world, selection);
        AABB boundingBox = shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds();
        CreateClient.OUTLINER.showAABB(slot, boundingBox.move(selection))
                .colored(color)
                .lineWidth(1 / 16f);
    }

    public static void drawOutline(BlockPos selection, Direction face, int color, String slot) {
        Level world = Minecraft.getInstance().level;
        if (selection == null)
            return;
        if(world == null)return;


        FaceVec3 faceVec3 = getFaceVec3(selection, face);
        CreateClient.OUTLINER.showLine(slot + "selection_l1", faceVec3.f1, faceVec3.f2)
                .colored(color)
                .lineWidth(1 / 16f);
        CreateClient.OUTLINER.showLine(slot + "selection_l2", faceVec3.f2, faceVec3.f3)
                .colored(color)
                .lineWidth(1 / 16f);
        CreateClient.OUTLINER.showLine(slot + "selection_l3", faceVec3.f3, faceVec3.f4)
                .colored(color)
                .lineWidth(1 / 16f);
        CreateClient.OUTLINER.showLine(slot + "selection_l4", faceVec3.f4, faceVec3.f1)
                .colored(color)
                .lineWidth(1 / 16f);
    }

    public static void textPlayerWhenHoldingWand(String text){
        if(!ClientWand.isClientWandInHand())return;
        if(!text.isEmpty())Minecraft.getInstance().gui.setOverlayMessage(Component.literal(text), true);
    }

    public static BlockEntity lookingAt(){
        if(Minecraft.getInstance().player == null)return null;
        HitResult hitResult = Minecraft.getInstance().player.pick(5, Minecraft.getInstance().getPartialTick(), false);

        if(hitResult.getType() == HitResult.Type.BLOCK){
            return Minecraft.getInstance().level.getExistingBlockEntity(((BlockHitResult) hitResult).getBlockPos());
        }
        return null;
    }

}
