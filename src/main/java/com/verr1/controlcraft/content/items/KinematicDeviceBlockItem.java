package com.verr1.controlcraft.content.items;

import com.verr1.controlcraft.foundation.type.descriptive.MiscDescription;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class KinematicDeviceBlockItem extends BlockItem {

    public KinematicDeviceBlockItem(Block p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.addAll(
                MiscDescription.KINEMATIC_TOOLTIP_MOTTO.specific().stream()
                        .map(c -> c.copy()
                                .withStyle(ChatFormatting.GOLD)
                                .withStyle(ChatFormatting.BOLD)
                        )
                        .toList()
        );

        tooltip.addAll(
                MiscDescription.CAUTION.specific().stream()
                        .map(c -> c.copy()
                                .withStyle(ChatFormatting.RED)
                                .withStyle(ChatFormatting.BOLD)
                        )
                        .toList()
        );

        tooltip.addAll(
                MiscDescription.KINEMATIC_TOOLTIP_CAUTION.specific().stream()
                        .map(c -> c.copy()
                                .withStyle(ChatFormatting.RED)
                        )
                        .toList()
        );

    }
}
