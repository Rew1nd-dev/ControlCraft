package com.verr1.vscontrolcraft.blocks.sphericalHinge;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.base.Hinge.*;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IAdjustableHinge;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.ICanBruteConnect;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IConstrainHolder;
import com.verr1.vscontrolcraft.base.Hinge.packets.HingeSyncClientPacket;
import com.verr1.vscontrolcraft.base.ShipConnectorBlockEntity;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSConstrainSerializeUtils;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Arrays;

public class SphericalHingeBlockEntity extends ShipConnectorBlockEntity implements
        ICanBruteConnect, IAdjustableHinge, IConstrainHolder
{

    protected Object attachment_ID;
    protected VSAttachmentConstraint attachment = null;


    public SphericalHingeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getDirection(){
        return getBlockState().getValue(SphericalHingeBlock.FACING);
    }

    @Override
    public void bruteConnectWith(BlockPos otherHingeBlockPos){
        if(level.isClientSide)return;
        if(!VSMathUtils.isOnServerShip(otherHingeBlockPos, (ServerLevel) level) && !isOnServerShip())return;
        if(!(level.getExistingBlockEntity(otherHingeBlockPos) instanceof SphericalHingeBlockEntity otherHinge))return;


        VSAttachmentConstraint attachment = new VSAttachmentConstraint(
                getServerShipID(),
                otherHinge.getServerShipID(),
                1.0E-10,
                getHingeConnectorPosJOML(),
                otherHinge.getHingeConnectorPosJOML(),
                1.0E10,
                0.0
        );

        recreateConstrains(attachment);
        setCompanionShipID(otherHinge.getServerShipID());
        notifyUpdate();
    }


    public void destroy() {
        super.destroy();
        if(level.isClientSide)return;
        destroyConstrain();

    }



    public void recreateConstrains(VSAttachmentConstraint Hinge)
    {
        this.attachment = Hinge;
        recreateConstrains();
    }

    public void recreateConstrains(){
        if(attachment == null)return;
        if(level.isClientSide)return;
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);
        attachment_ID =  shipWorldCore.createNewConstraint(this.attachment);

        if(attachment_ID == null){
            attachment = null;
        }
    }

    private Vector3d getHingeConnectorPosJOML() {
        return Util.Vec3toVector3d(getBlockPos().getCenter())
                .fma(-0.5 + getHingeLevel().correspondLength(), getDirectionJOML());

    }



    public void writeSavedConstrains(CompoundTag tag){
        tag.putBoolean("assembled", getCompanionServerShip() != null);
        if(attachment == null)return;
        tag.putString("assemDir", getCompanionShipDirection().getSerializedName());
        tag.putLong("asm", getCompanionShipID());
        tag.putLong("own", getServerShipID());
        VSConstrainSerializeUtils.writeVSAttachmentConstrain(tag, "attach_", attachment);

    }


    public void readSavedConstrains(CompoundTag tag){
        boolean assembled = tag.getBoolean("assembled");
        if(!assembled)return;
        String assemDirString = tag.getString("assemDir");
        setCompanionShipDirection(Direction.byName(assemDirString));;
        setCompanionShipID(tag.getLong("asm"));
        attachment = VSConstrainSerializeUtils.readVSAttachmentConstrain(tag, "attach_");
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(clientPacket)return;
        writeSavedConstrains(tag);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if(clientPacket)return;
        try {
            readSavedConstrains(tag);
            DeferralExecutor.executeLater(this::recreateConstrains, 1);
        }catch (Exception e){
            ControlCraft.LOGGER.info("Failed to read saved constrains");
        }

    }

    public HingeAdjustLevel getHingeLevel(){
        return getBlockState().getValue(SphericalHingeBlock.LEVEL);
    }

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
    public void destroyConstrain() {
        if(level.isClientSide)return;
        try{
            var shipWorldCore = (ShipObjectServerWorld) VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);
            if(attachment_ID != null)shipWorldCore.removeConstraint((int)attachment_ID);
            attachment_ID = null;
        }catch (Exception e){
            ControlCraft.LOGGER.error(Arrays.toString(e.getStackTrace()));
        }

    }




}

