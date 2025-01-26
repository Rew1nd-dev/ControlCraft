package com.verr1.vscontrolcraft.base.Hinge;

import com.verr1.vscontrolcraft.base.Hinge.interfaces.IAdjustableHinge;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.ICanBruteConnect;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IConstrainHolder;
import com.verr1.vscontrolcraft.base.Hinge.packets.HingeSyncClientPacket;
import com.verr1.vscontrolcraft.base.ShipConnectorBlockEntity;
import com.verr1.vscontrolcraft.blocks.sphericalHinge.SphericalHingeBlock;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

public abstract class AbstractHingeBlockEntity extends ShipConnectorBlockEntity implements
        ICanBruteConnect, IConstrainHolder, IAdjustableHinge {

    public AbstractHingeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract HingeAdjustLevel getHingeLevel();

    @Override
    public void adjust() {
        setAdjustment(getHingeLevel().next());
        syncToClient();
    }

    @Override
    public HingeAdjustLevel getAdjustment() {
        return getHingeLevel();
    }

    @Override
    public void setAdjustment(HingeAdjustLevel hingeLevel) {
        updateBlockState(level, getBlockPos(), getBlockState().setValue(SphericalHingeBlock.LEVEL, hingeLevel));
    }


    void syncToClient(){
        if(level.isClientSide)return;
        AllPackets
                .getChannel()
                .send(PacketDistributor.ALL.noArg(),new HingeSyncClientPacket(getBlockPos(), getAdjustment(), false));
    }

    @Override
    public abstract void bruteConnectWith(BlockPos otherBlockPos);

    @Override
    public abstract void destroyConstrain();

    public void destroy() {
        super.destroy();
        if(level.isClientSide)return;
        destroyConstrain();

    }
}
