package com.verr1.controlcraft.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Contraption.class)
public abstract class MixinContraption {

    @Shadow(remap = false)
    protected abstract BlockPos toLocalPos(BlockPos globalPos);

    @Inject(method = "getBlockEntityNBT", at = @At("HEAD"), cancellable = true, remap = false)
    void controlCraft$getBlockEntityNBT(Level world, BlockPos pos, CallbackInfoReturnable<CompoundTag> cir){
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null){
            cir.setReturnValue(null);
            cir.cancel();
            return;
        }
        CompoundTag nbt = blockEntity.saveWithFullMetadata();

        // just don't remove the x, y, z tags, I really need it :<

        if(!(blockEntity instanceof SpatialAnchorBlockEntity spatial)){
            nbt.remove("x");
            nbt.remove("y");
            nbt.remove("z");
        }else{
            nbt.putLong("protocol", spatial.getProtocol());
        }

        if ((blockEntity instanceof FluidTankBlockEntity || blockEntity instanceof ItemVaultBlockEntity)
                && nbt.contains("Controller"))
            nbt.put("Controller",
                    NbtUtils.writeBlockPos(toLocalPos(NbtUtils.readBlockPos(nbt.getCompound("Controller")))));

        cir.setReturnValue(nbt);
        cir.cancel();
    }


}
