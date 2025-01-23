package com.verr1.vscontrolcraft.base.Servo;


import com.simibubi.create.CreateClient;
import com.verr1.vscontrolcraft.registry.AllItems;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ServoMotorAssembleHandler {
    static BlockPos assemPos;
    static Direction assemDir;
    static BlockPos servoPos;
    static Direction servoDir;
    static ItemStack currentItem;


    /*
    * @SubscribeEvent
    public static void rightClickingBlocksSelects(PlayerInteractEvent.RightClickBlock event) {
        if (currentItem.getItem() != AllItems.ALL_IN_WAND.get())return;

        BlockPos pos = event.getPos();
        Level world = event.getLevel();
        if (!world.isClientSide)
            return;
        Player player = event.getEntity();
        if (player == null || player.isSpectator())
            return;


        if (event.getLevel().getExistingBlockEntity(pos) instanceof AbstractServoMotor asv) {
            servoPos = asv.getBlockPos();
            servoDir = asv.getDirection();
        } else if (!(event.getLevel().getBlockState(pos).getBlock() instanceof AirBlock)) {
            assemPos = pos;
            assemDir = event.getFace();
        } else{
            flush();
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void leftClickingBlocksConfirmsSelection(PlayerInteractEvent.RightClickEmpty event) {
        if (currentItem.getItem() != AllItems.ALL_IN_WAND.get())return;

        if(assemPos != null && servoPos != null && !event.getEntity().isShiftKeyDown()){
            AllPackets.getChannel().sendToServer(new ServoMotorConstrainAssemblePacket(assemPos, servoPos, assemDir, servoDir));
            flush();
        }else if(event.getEntity().isShiftKeyDown()){
            flush();
        }


        event.setCancellationResult(InteractionResult.SUCCESS);

    }

    **/

    public static void flush(){
        assemPos = null;
        servoPos = null;
    }

    public static void tick() {
        Player player = Minecraft.getInstance().player;

        if (player == null)
            return;

        currentItem = player.getMainHandItem();

        drawOutline(assemPos, 0xffcb74, "target");
        drawOutline(servoPos, 0xaaca32, "source");
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
