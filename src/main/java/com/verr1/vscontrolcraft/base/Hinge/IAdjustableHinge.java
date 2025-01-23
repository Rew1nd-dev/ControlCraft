package com.verr1.vscontrolcraft.base.Hinge;

import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;

public interface IAdjustableHinge {

    void adjust();

    HingeAdjustLevel getAdjustment();

    void setAdjustment(HingeAdjustLevel level);


}
