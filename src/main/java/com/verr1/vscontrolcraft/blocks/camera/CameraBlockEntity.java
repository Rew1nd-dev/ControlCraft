package com.verr1.vscontrolcraft.blocks.camera;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.base.DataStructure.ShipHitResult;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldWrapper;
import com.verr1.vscontrolcraft.base.UltraTerminal.ITerminalDevice;
import com.verr1.vscontrolcraft.base.UltraTerminal.WidgetType;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.CameraPeripheral;
import com.verr1.vscontrolcraft.mixinDuck.EntityAccessor;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.ClipUtils;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

import java.lang.Math;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CameraBlockEntity extends OnShipDirectinonalBlockEntity
        implements IPacketHandler, ITerminalDevice, IHaveGoggleInformation
{
    public ShipHitResult latestShipHitResult = null;
    public EntityHitResult latestEntityHitResult = null;
    public EntityHitResult latestServerPlayerHitResult = null;
    public BlockHitResult latestBlockHitResult = null;


    private double pitch = 0; // in degree
    private double yaw = 0;

    private CameraPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private double clipRange = 256;

    private String userUUID = "None";

    private boolean receivedSignalChanged = false;
    private int lastOutputSignal = 0;

    private boolean isActiveDistanceSensor = false;

    List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    () -> (isActiveDistanceSensor() ? 1.0 : 0.0),
                    v -> {}, // This field is used to output redstone, so no input here
                    "Sensor",
                    WidgetType.TOGGLE,
                    ExposedFieldType.IS_SENSOR
            )
    );

    public void clipNewShip(){
        latestShipHitResult = clipShip();
    }

    public void clipNewEntity(){
        latestEntityHitResult = clipEntity(Entity::isAlive);
    }

    public void clipNewServerPlayer(){
        latestServerPlayerHitResult = clipServerPlayer();
    }

    public void clipNewBlock(){
        latestBlockHitResult = clipBlock(false);
    }

    public double getClipRange() {
        return clipRange;
    }

    public void setClipRange(double clipRange) {
        this.clipRange = clipRange;
    }

    public boolean isActiveDistanceSensor() {
        return isActiveDistanceSensor;
    }

    public void setActiveDistanceSensor(boolean activeDistanceSensor) {
        isActiveDistanceSensor = activeDistanceSensor;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public void updateNeighbor(){
        if(level == null)return;
        if (!receivedSignalChanged)return;
        receivedSignalChanged = false;
        Arrays
            .stream(Direction.values())
            .filter(fields.get(0).directionOptional::test)
            .forEach(
                    face -> {
                        BlockPos attachedPos = worldPosition.relative(face);
                        level.blockUpdated(worldPosition, level.getBlockState(worldPosition)
                                .getBlock());
                        level.blockUpdated(attachedPos, level.getBlockState(attachedPos)
                                .getBlock());
                    }
            );
    }

    public double getClipDistance(){
        BlockHitResult r = clipBlock(true);
        if(r == null)return 0;
        Vector3d p = getCameraPosition();
        Vector3d q = Util.Vec3toVector3d(r.getLocation());
        double d = p.distance(q);
        return d;
    }

    public int getOutputSignal(){
        return lastOutputSignal;
    }

    public void updateOutputSignal(){
        if(!isActiveDistanceSensor)return;
        if(level.isClientSide)return;
        // if(!fields.get(0).directionOptional.test(side))return 0;

        double d = getClipDistance();

        double a = fields.get(0).min_max.x;
        double b = fields.get(0).min_max.y;
        double ratio = Util.clampHalf(
                Math.abs(d - a) / (Math.abs(a - b) + 1e-8), 1
        );

        int newSignal = (int)(ratio * 15);
        if(newSignal != lastOutputSignal){
            receivedSignalChanged = true;
            lastOutputSignal = newSignal;
        }

    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public void setPitchYaw(double pitch, double yaw){
        setPitch(pitch);
        setYaw(yaw);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLinkedCamera(){
        return (LinkedCameraManager.isIsLinked() && LinkedCameraManager.getLinkCameraPos().equals(getBlockPos()));
    }

    @OnlyIn(Dist.CLIENT)
    public void setPitchForceClient(double pitch){
        setPitch(pitch);
        if(!isLinkedCamera())return;
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)return;
        player.setXRot((float)pitch);
    }

    @OnlyIn(Dist.CLIENT)
    public void setYawForceClient(double yaw){
        setYaw(yaw);
        if(!isLinkedCamera())return;
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)return;
        player.setYRot((float)yaw);
    }

    public void setPitchYawForceServer(double pitch, double yaw){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_1)
                .withDouble(pitch)
                .withDouble(yaw)
                .build();

        AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
    }


    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public CameraBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new CameraPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }



    private ClipContext clipContext(){
        Vector3dc camPos_wc = VSMathUtils.getAbsolutePosition(level, worldPosition);
        Vector3dc camFront_wc = getAbsViewForward();
        Vector3dc camStart_wc = camPos_wc.add(camFront_wc, new Vector3d());
        Vector3dc camTo_wc = camStart_wc.fma(clipRange, camFront_wc, new Vector3d());
        return new ClipContext(
                Util.Vector3dToVec3(camStart_wc),
                Util.Vector3dToVec3(camTo_wc),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                null
        );
    }


    private AABB clipAABB(){
        Vector3dc center = VSMathUtils.getAbsolutePosition(level, worldPosition);
        Vector3dc view = getAbsViewForward();
        Vector3dc camMin = center.fma(clipRange, view, new Vector3d());
        Vector3dc camMax = center.fma(-10, view, new Vector3d());
        return new AABB(
                Util.Vector3dToVec3(camMin),
                Util.Vector3dToVec3(camMax)
        );
    }



    public void resetView(){
        if(level.isClientSide)return;
        switch (getDirection()){
            case UP -> setPitchYawForceServer(-90, 0);
            case DOWN -> setPitchYawForceServer(90, 0);
            case SOUTH -> setPitchYawForceServer(0, 0);
            case NORTH -> setPitchYawForceServer(0, 180);
            case EAST -> setPitchYawForceServer(0, -90);
            case WEST -> setPitchYawForceServer(0, 90);
        }
    }


    public BlockHitResult transformToWorld(@Nullable BlockHitResult hitResult, Level level){
        if(hitResult == null)return null;
        if(!VSGameUtilsKt.isBlockInShipyard(level, hitResult.getBlockPos()))return hitResult;
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, hitResult.getBlockPos());
        if(ship == null)return hitResult;
        Vector3d v = ship.getTransform().getShipToWorld().transformPosition(Util.Vec3toVector3d(hitResult.getLocation()));
        return new BlockHitResult(
                new Vec3(v.x, v.y, v.z),
                hitResult.getDirection(),
                hitResult.getBlockPos(),
                hitResult.isInside()
        );
    }


    public @Nullable BlockHitResult clipBlock(Vector3d from, Vector3d to){
        if(level == null)return null;

        ClipContext context = new ClipContext(
                Util.Vector3dToVec3(from),
                Util.Vector3dToVec3(to),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                null
        );
        return RaycastUtilsKt.clipIncludeShips(level, context);
    }

    public @Nullable ShipHitResult clipShip(){
        ClipContext context = clipContext();
        return ClipUtils.clipShip(
                context.getFrom(),
                context.getTo(),
                clipAABB(),
                3,
                level,
                s -> !Optional.ofNullable(getShipOn()).map(Ship::getId).orElse(0L).equals(s.getId())
        );
    }

    public @Nullable EntityHitResult clipServerPlayer(){
        ClipContext context = clipContext();
        return ClipUtils.clipServerPlayer(
                context.getFrom(),
                context.getTo(),
                clipAABB(),
                3,
                (ServerLevel)level,
                p -> true
        );
    }

    public @Nullable BlockHitResult clipBlock(boolean transformToWorld){
        if(level == null)return null;

        return RaycastUtilsKt.clipIncludeShips(level, clipContext(), transformToWorld, getShipID());
    }

    public @Nullable EntityHitResult clipEntity(Predicate<Entity> filter){
        if(level == null)return null;
        ClipContext context = clipContext();

        /*
        * RaycastUtilsKt.raytraceEntities(
                level,
                new Arrow(EntityType.ARROW, level),
                context.getFrom(),
                context.getTo(),
                clipAABB(),
                (e) -> true,
                clipRange
        );
        * */

        return ClipUtils.clipEntity(
                context.getFrom(),
                context.getTo(),
                clipAABB(),
                0.5,
                level,
                filter
        );
    }


    @OnlyIn(Dist.CLIENT)
    public void outlineViewClip(){
        BlockHitResult hitResult = clipBlock(false);
        if(hitResult == null)return;
        if(level == null)return;
        /*
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos);
        AABB boundingBox = shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds();
        CreateClient.OUTLINER.showAABB("camera_clip", boundingBox.move(pos))
                .colored(Color.RED)
                .lineWidth(1 / 16f);
        * */

        double sign_x = hitResult.getDirection().getAxis() == Direction.Axis.X ? 0 : 1;
        double sign_y = hitResult.getDirection().getAxis() == Direction.Axis.Y ? 0 : 1;
        double sign_z = hitResult.getDirection().getAxis() == Direction.Axis.Z ? 0 : 1;


        outlineLocation(
                hitResult.getLocation().add(new Vec3(-0.5 * sign_x, -0.5 * sign_y, -0.5 * sign_z)),
                hitResult.getDirection().getOpposite(),
                Color.RED.getRGB(),
                "camera_clip"
        );
    }

    @OnlyIn(Dist.CLIENT)
    public void outlineEntityClip(){
        EntityHitResult hitResult = clipEntity((e) -> true);
        if(hitResult == null)return;
        if(level == null)return;
        if(hitResult.getEntity() instanceof EntityAccessor lv){
            lv.controlCraft$setClientGlowing(4);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void outlineClipRay(){
        ClipContext clipContext = clipContext();
        CreateClient.OUTLINER.showLine("camera_clip_ray", clipContext.getFrom(), clipContext.getTo())
                .colored(Color.WHITE)
                .lineWidth(1 / 16f);
    }

    @OnlyIn(Dist.CLIENT)
    public void outlineShipClip(){
        ShipHitResult hitResult = clipShip();
        if(hitResult == null)return;
        if(level == null)return;
        double distance = hitResult.ship().getTransform().getPositionInWorld().distance(getCameraPosition());
        WandRenderer.drawOutline(
                VectorConversionsMCKt.toMinecraft(hitResult.ship().getWorldAABB()),
                Color.SPRING_GREEN.getRGB(),
                "camera_clip_ship",
                distance / 5
        );
    }

    @OnlyIn(Dist.CLIENT)
    private void outlineLocation(Vec3 center, Direction direction, int color, String slot){
        double distance = center.subtract(Util.Vector3dToVec3(getCameraPosition())).length();
        double scale = distance / 5;
        WandRenderer.drawOutline(center, direction, scale, color, slot);
    }

    @OnlyIn(Dist.CLIENT)
    public void outlineExtra(Vec3 pos, Direction direction, String slot, int rgb){
        if(level == null)return;
        if(!LinkedCameraManager.isIsLinked())return;
        if(!LinkedCameraManager.getLinkCameraPos().equals(getBlockPos()))return;
        outlineLocation(pos, direction, rgb, slot);
    }


    public void outlineExtraTo(CameraDrawingContext context){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_0)
                .withDouble(context.pos.x)
                .withDouble(context.pos.y)
                .withDouble(context.pos.z)
                .withUtf8(context.dir.name())
                .withUtf8(context.slot)
                .withLong(context.rgb)
                .build();

        AllPackets.sendToPlayer(p, context.player);
    }

    public void outlineExtraToUser(Vec3 pos, Direction dir, String slot, int rgb){
        if(level.isClientSide)return;
        ServerPlayer player = ((ServerLevel)level)
                .players()
                .stream()
                .filter(p -> p.getName().getString().equals(userUUID))
                .findFirst()
                .orElse(null);
        if(player == null)return;
        CameraDrawingContext ctx = new CameraDrawingContext(player, pos, dir, slot, rgb);
        outlineExtraTo(ctx);
    }


    public Vector3d getCameraPositionShip(){
        Vec3 front = getBlockPos().getCenter();
        Vector3d front3d_wc = new Vector3d(front.x, front.y, front.z);
        return front3d_wc;
    }

    public ClientShip getClientShip(){
        ClientShip ship = (ClientShip)VSGameUtilsKt.getShipManagingPos(level, getBlockPos());
        return ship;
    }

    public Vector3d getCameraPosition(){
        return VSMathUtils.getAbsolutePosition(level, worldPosition);
    }

    public Quaterniondc getCameraBaseRotation(){
        Quaterniond q = new Quaterniond();
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, getBlockPos());
        if(ship == null)return q;
        return ship.getTransform().getShipToWorldRotation();
    }

    @Override
    public void tick() {
        super.tick();
        if(isActiveDistanceSensor()){
            //setChanged();
            updateOutputSignal();
            updateNeighbor();
        }

        if(level.isClientSide){

            return;
        }
        syncClient(getBlockPos(), level);
    }



    public Quaterniond getAbsViewTransform(){
        Quaterniondc originalRotation =
                new Quaterniond().rotateY(Math.toRadians(-yaw)).rotateX(Math.toRadians(pitch)).normalize();
        return getCameraBaseRotation().mul(originalRotation, new Quaterniond());
    }

    public Vector3d getAbsViewForward(){
        return getAbsViewTransform().transform(new Vector3d(0, 0, 1));
    }

    public Quaterniond getLocViewTransform(){
        return new Quaterniond().rotateY(Math.toRadians(-yaw)).rotateX(Math.toRadians(pitch)).normalize();
    }

    public Vector3d getLocViewForward(){
        return getLocViewTransform().transform(new Vector3d(0, 0, 1));
    }


    public void syncServer(String uuid){
        if(!level.isClientSide)return;
        var p = new BlockBoundServerPacket.builder(getBlockPos(), BlockBoundPacketType.SETTING_0)
                .withDouble(pitch)
                .withDouble(yaw)
                .withUtf8(uuid)
                .build();
        AllPackets.getChannel().sendToServer(p);
    }

    public void displayScreen(ServerPlayer player){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN_0)
                .withBoolean(isActiveDistanceSensor)
                .build();

        AllPackets.sendToPlayer(p, player);
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING_0){
            double p = packet.getDoubles().get(0);
            double y = packet.getDoubles().get(1);
            String uuid = packet.getUtf8s().get(0);
            setPitch(p);
            setYaw(y);
            setUserUUID(uuid);
        }
        if(packet.getType() == BlockBoundPacketType.SETTING_1){
            setActiveDistanceSensor(packet.getBooleans().get(0));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SYNC_0){
            Vec3 pos = new Vec3(
                    packet.getDoubles().get(0),
                    packet.getDoubles().get(1),
                    packet.getDoubles().get(2)
            );
            Direction dir = Direction.valueOf(
                    packet.getUtf8s().get(0)
            );
            String slot =
                    packet.getUtf8s().get(1);
            int rgb =
                    packet.getLongs().get(0).intValue();
            outlineExtra(pos, dir, slot, rgb);
        }
        if(packet.getType() == BlockBoundPacketType.SYNC_1){
            double p = packet.getDoubles().get(0);
            double y = packet.getDoubles().get(1);
            setPitchForceClient(p);
            setYawForceClient(y);
        }
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN_0){
            boolean isActive = packet.getBooleans().get(0);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new CameraScreen(getBlockPos(), isActive)
            ));
        }

    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if(clientPacket)return;
        pitch = compound.getDouble("pitch");
        yaw = compound.getDouble("yaw");
        isActiveDistanceSensor = compound.getBoolean("is_active_distance_sensor");
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if(clientPacket)return;
        compound.putDouble("pitch", pitch);
        compound.putDouble("yaw", yaw);
        compound.putBoolean("is_active_distance_sensor", isActiveDistanceSensor);
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "camera";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        Direction dir = WandRenderer.lookingAtFaceDirection();
        if(dir == null)return true;
        tooltip.add(Components.literal("    Face " + dir + " Bounded:"));
        fields().forEach(f -> {
            if(!f.directionOptional.test(dir))return;
            String info = f.type.getComponent().getString();
            tooltip.add(Component.literal(info).withStyle(ChatFormatting.AQUA));
        });

        return true;
    }

    public record CameraDrawingContext(ServerPlayer player, Vec3 pos, Direction dir, String slot, int rgb) { }
}
