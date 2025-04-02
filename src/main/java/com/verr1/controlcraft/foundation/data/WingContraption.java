package com.verr1.controlcraft.foundation.data;


import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

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
        return true;
    }


}
