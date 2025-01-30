package com.verr1.vscontrolcraft.blocks.wingController;


import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class WingContraption extends BearingContraption {

    public WingContraption(Direction facing){
        super(false, facing);
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        super.assemble(world, pos);
        return containOnlyWings();
    }

    private boolean containOnlyWings(){
        boolean onlyWings =   blocks
                                .entrySet()
                                .stream()
                                .allMatch(
                                        entry -> entry
                                                .getValue()
                                                .state()
                                                .getBlock() instanceof KineticBlock // This is only for Debugging
                                );
        // if(!onlyWings)throw AssemblyException("Only Wing Blocks Allowed", )
        // boolean onlyOneLayer = blocks.entrySet().stream().anyMatch(entry->entry.getValue().pos())
        return true;
    }


}
