package com.verr1.vscontrolcraft.blocks.spinalyzer;


import com.simibubi.create.CreateClient;
import com.verr1.vscontrolcraft.registry.AllItems;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class SpinalyzerTargetHandler {
    static BlockPos receiverPos;
    static BlockPos transmitterPos;
    static ItemStack currentItem;
    /*
    @SubscribeEvent
    public static void rightClickingBlocksSelectsTransmitter(PlayerInteractEvent.RightClickBlock event) {
        if (currentItem.getItem() != AllItems.LINKER.get())return;

        BlockPos pos = event.getPos();
        Level world = event.getLevel();
        if (!world.isClientSide)
            return;
        Player player = event.getEntity();
        if (player == null || player.isSpectator())
            return;


        if (event.getLevel().getBlockState(pos).getBlock() instanceof SpinalyzerBlock) {
            if (player.isShiftKeyDown()) transmitterPos = pos;
            if (!player.isShiftKeyDown()) receiverPos = pos;
        }
        else{
            flush();
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void leftClickingBlocksConfirmsSelection(PlayerInteractEvent.RightClickEmpty event) {
        if (currentItem.getItem() != AllItems.LINKER.get())return;

        if(receiverPos != null && transmitterPos != null && !event.getEntity().isShiftKeyDown()){
            AllPackets.getChannel().sendToServer(new SpinalyzerLinkPacket(receiverPos, transmitterPos));
            flush();
        }else if(event.getEntity().isShiftKeyDown()){
            flush();
        }


        event.setCancellationResult(InteractionResult.SUCCESS);

    }
    * */


    public static void flush(){
        receiverPos = null;
        transmitterPos = null;
    }

    public static void tick() {
        Player player = Minecraft.getInstance().player;

        if (player == null)
            return;

        currentItem = player.getMainHandItem();

        drawOutline(receiverPos, 0xffcb74, "target");
        drawOutline(transmitterPos, 0xaaca32, "source");
    }

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
