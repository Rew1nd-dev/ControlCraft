package com.verr1.controlcraft.content.items;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.gui.screens.CameraCreateLinkScreen;
import com.verr1.controlcraft.content.gui.screens.CameraLinkScreen;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CameraLinkItem extends Item {

    public CameraLinkItem(Properties p_41383_) {
        super(p_41383_);
    }

    @OnlyIn(Dist.CLIENT)
    public void displayCreateLinkScreen(BlockPos pos){
        ScreenOpener.open(new CameraCreateLinkScreen(pos));
    }

    @OnlyIn(Dist.CLIENT)
    public void displayLinkScreen(){
        ScreenOpener.open(new CameraLinkScreen(Component.literal("Camera Link")));
    }


    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if(!context.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(context.getHand() != InteractionHand.MAIN_HAND)return InteractionResult.SUCCESS;

        if(context.getLevel().getBlockState(context.getClickedPos()).is(ControlCraftBlocks.CAMERA_BLOCK.get())){
            displayCreateLinkScreen(context.getClickedPos());
        }else{
            displayLinkScreen();
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide)return super.use(level, player, hand);
        if(hand != InteractionHand.MAIN_HAND)return super.use(level, player, hand);

        displayLinkScreen();

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
