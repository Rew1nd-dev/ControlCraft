package com.verr1.vscontrolcraft.blocks.jetRudder;

import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.blocks.jet.JetBlockEntity;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class JetRudderBlockEntity extends OnShipDirectinonalBlockEntity implements
        IPacketHandler
{

    public LerpedFloat animatedHorizontalAngle = LerpedFloat.angular();
    public float targetHorizontalAngle = 0;
    public LerpedFloat animatedVerticalAngle = LerpedFloat.angular();
    public float targetVerticalAngle = 0;
    public float targetThrust = 0;


    public Direction getFiexdDirection() {
        return getDirection().getOpposite();
    }

    public Direction getVertical(){
        BlockEntity be = level.getExistingBlockEntity(getBlockPos().relative(getDirection().getOpposite()));
        if(!(be instanceof JetBlockEntity jet))return Direction.NORTH;
        return jet.getVertical();
    }

    // jet rudder's direction is the opposite of jet
    public Direction getHorizontal(){
        BlockEntity be = level.getExistingBlockEntity(getBlockPos().relative(getDirection().getOpposite()));
        if(!(be instanceof JetBlockEntity jet))return Direction.NORTH;
        return jet.getHorizontal();
    }

    public Vector3d getVerticalJOML(){
        return Util.Vec3itoVector3d(getVertical().getNormal());
    }

    public Vector3d getHorizontalJOML(){
        return Util.Vec3itoVector3d(getHorizontal().getNormal());
    }

    public JetRudderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setAnimatedAngles(double horizontal, double vertical, double thrust){
        targetHorizontalAngle = (float) VSMathUtils.clamp(horizontal, Math.toRadians(45));
        targetVerticalAngle = (float) VSMathUtils.clamp(vertical, Math.toRadians(45));
        targetThrust = (float)thrust;
    }


    public Vector2d getRenderAngles(){



        int h_dumbFix = 1;
        if(getDirection() == Direction.SOUTH || getDirection() == Direction.EAST || getDirection() == Direction.UP){
            h_dumbFix = -1;
        }

        float h = animatedHorizontalAngle.getValue(1) * h_dumbFix;
        float v = animatedVerticalAngle.getValue(1);

        double sh = Math.sin(h) / Math.sqrt(2);
        double sv = Math.sin(v) / Math.sqrt(2);
        double st = Math.sqrt(Math.abs(1 - (sh * sh + sv * sv))); // in case of < 0
        double rh = Math.atan2(sh, st);
        double rv = Math.atan2(sv, st);

        return new Vector2d(rh, rv);
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide) {
            tickAnimation();
            tickParticles();
        }else{
            syncClient();
        }

    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        checkDrivenJet();
    }

    public void checkDrivenJet(){
        if(level.isClientSide)return;
        BlockPos jetPos = getBlockPos().relative(getDirection().getOpposite());
        if(!(level.getExistingBlockEntity(jetPos) instanceof JetBlockEntity jet)){
            setAnimatedAngles(0, 0, 0);
        }
    }


    public void tickParticles(){
        if(!level.isClientSide)return;
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, getBlockPos());


        Vector3d dir = getRenderThrustDir().mul(-1);

        Vector3d p_wc = Util.Vec3toVector3d(getBlockPos().getCenter()).fma(0.5, getDirectionJOML());
        Vector3d v_wc = dir.mul(Util.clamp1(targetThrust * 1e-3) * 3, new Vector3d());

        if(v_wc.lengthSquared() < 1e-2)return;

        Vector3d extraVelocity = new Vector3d();

        if(ship != null){
            ship.getTransform().getShipToWorld().transformPosition(p_wc);
            ship.getTransform().getShipToWorld().transformDirection(v_wc);
            Vector3d r_sc = Util.Vec3itoVector3d(getBlockPos().relative(getDirection())).sub(ship.getTransform().getPositionInShip());
            Vector3d r_wc = ship.getTransform().getShipToWorld().transformDirection(r_sc);
            extraVelocity = ship.getOmega().cross(r_wc, new Vector3d()).add(ship.getVelocity());
        }
        v_wc.add(extraVelocity.mul(0.05));

        addParticles(p_wc, v_wc);

    }

    private void addParticles(Vector3dc p_wc, Vector3dc v_wc){
        if(level == null)return;
        //if(v_wc.lengthSquared() < 0.1)return;
        double scale = v_wc.length();
        Vector3d dir = v_wc.normalize(new Vector3d()).mul(scale);
        double spread = 0.1;
        level.addParticle(
                ParticleTypes.CLOUD,
                p_wc.x() + (2 * level.random.nextDouble() - 1) * spread,
                p_wc.y() + (2 * level.random.nextDouble() - 1) * spread,
                p_wc.z() + (2 * level.random.nextDouble() - 1) * spread,
                dir.x(),
                dir.y(),
                dir.z()
        );

    }



    private Vector3d getRenderThrustDir() {
        Vector3dc basis_h = getHorizontalJOML();
        Vector3dc basis_v = getVerticalJOML();
        Vector3dc basis_t = getDirectionJOML().mul(-1);  // make it the opposite (set to bounded jet direction)

        float h = targetHorizontalAngle;
        float v = targetVerticalAngle;

        return JetBlockEntity.getThrustDir(h, v, basis_h, basis_v, basis_t);
    }


    public void syncClient() {
        if(!level.isClientSide){
            var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_0)
                    .withDouble(targetHorizontalAngle)
                    .withDouble(targetVerticalAngle)
                    .withDouble(targetThrust)
                    .build();


            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickAnimation(){
        animatedHorizontalAngle.chase(targetHorizontalAngle , 0.1, LerpedFloat.Chaser.EXP);
        animatedVerticalAngle.chase(targetVerticalAngle , 0.1, LerpedFloat.Chaser.EXP);
        animatedHorizontalAngle.tickChaser();
        animatedVerticalAngle.tickChaser();
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SYNC_0){
            double h = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double t = packet.getDoubles().get(2);
            setAnimatedAngles(h, v, t);
        }
    }

}
