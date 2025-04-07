package com.verr1.controlcraft.foundation.managers;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.content.blocks.OptionalSyncedBlockEntity;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlockEntity;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.executor.ExpirableConditionRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

import static com.verr1.controlcraft.ControlCraftClient.CLIENT_DEFERRAL_EXECUTOR;
import static com.verr1.controlcraft.content.blocks.anchor.AnchorBlockEntity.*;

@OnlyIn(Dist.CLIENT)
public class ClientGUIManager {

    public void tryOpenGUIAtBlock(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        Optional
                .ofNullable(mc.level)
                .map(lvl -> lvl.getExistingBlockEntity(pos))
                .filter(OnShipBlockEntity.class::isInstance);
    }

    private void OpenAnchorGUI(AnchorBlockEntity anchor){
        SyncOptionalSyncedBlockEntity(anchor, AIR_RESISTANCE, EXTRA_GRAVITY, ROTATIONAL_RESISTANCE);
        CLIENT_DEFERRAL_EXECUTOR.executeLater(SimpleScreenTaskFactory(anchor, AIR_RESISTANCE, EXTRA_GRAVITY, ROTATIONAL_RESISTANCE));
    }

    /*
    *   TODO: These should be in the screen class
    *
    * */


    private static void SyncOptionalSyncedBlockEntity(OptionalSyncedBlockEntity be, NetworkKey... keys){
        be.request(keys);
    }

    private static ExpirableConditionRunnable SimpleScreenTaskFactory(OptionalSyncedBlockEntity be, NetworkKey... keys){
        return new ExpirableConditionRunnable
                .builder(() -> {}/*ScreenOpener.open(new AnchorScreen(be.getBlockPos()))*/)
                .withCondition(() -> !be.isAnyDirty(keys))
                .withExpirationTicks(20)
                .withOrElse(() -> Optional
                        .ofNullable(Minecraft.getInstance().player)
                        .ifPresent(p -> p.sendSystemMessage(Component.literal("Block Entity Data Is Not Synced"))))
                .build();
    }
}
