package com.verr1.controlcraft.content.cctweaked.peripheral;

import com.verr1.controlcraft.content.blocks.camera.CameraBlockEntity;
import com.verr1.controlcraft.foundation.data.ShipHitResult;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.utils.CCUtils;
import com.verr1.controlcraft.utils.MinecraftUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

public class CameraPeripheral extends AbstractAttachedPeripheral<CameraBlockEntity>{
    public CameraPeripheral(CameraBlockEntity target) {
        super(target);
    }

    @Override
    public String getType() {
        return "camera";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof CameraPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public final Map<String, Double> getAbsViewTransform(){
        return CCUtils.dumpVec4(getTarget().getAbsViewTransform());
    }

    @LuaFunction
    public final String getDirection(){
        return getTarget().getDirection().getName();
    }

    @LuaFunction
    public final Map<String, Double> getCameraPosition(){
        return CCUtils.dumpVec3(getTarget().getCameraPosition());
    }

    @LuaFunction
    public final Map<String, Double> getAbsViewForward(){
        return CCUtils.dumpVec3(getTarget().getAbsViewForward());
    }

    @LuaFunction
    public final Map<String, Double> getLocViewTransform(){
        return CCUtils.dumpVec4(getTarget().getLocViewTransform());
    }

    @LuaFunction
    public final Map<String, Double> getLocViewForward(){
        return CCUtils.dumpVec3(getTarget().getLocViewForward());
    }

    @LuaFunction
    public final void outlineToUser(
            double x, double y, double z,
            String direction,
            int color,
            String slot
    ){
        getTarget().outlineExtraToUser(new Vec3(x, y, z), Direction.valueOf(direction), slot, color);
    }

    @LuaFunction
    public final void forcePitchYaw(double pitch, double yaw){
        getTarget().setPitchYawForceServer(pitch, yaw);
    }

    @LuaFunction
    public final Map<String, Object> clip(){
        BlockHitResult hitResult = getTarget().clipBlock(true);
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult);
    }

    @LuaFunction
    public final Map<String, Object> clipEntity(){
        EntityHitResult hitResult = getTarget().clipEntity(Entity::isAlive);
        if(hitResult == null)return null;
        return Map.of(
                "hit", Map.of(
                        "x", hitResult.getLocation().x,
                        "y", hitResult.getLocation().y,
                        "z", hitResult.getLocation().z
                ),
                "type", hitResult.getEntity().getType().toString(),
                "name", hitResult.getEntity().getName().getString()
        );
    }

    @LuaFunction
    public final Map<String, Object> clipBlockDetail(){
        BlockHitResult hitResult = getTarget().clipBlock(true);
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult, getTarget().getLevel());
    }

    @LuaFunction
    public final Map<String, Object> clipEntityDetail(){
        EntityHitResult hitResult = getTarget().clipEntity((e) -> true);
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult);
    }

    @LuaFunction
    public final Map<String, Object> clipShipDetail(){
        ShipHitResult hitResult = getTarget().clipShip();
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult);
    }

    @LuaFunction
    public final Map<String, Object> clipPlayerDetail(){
        EntityHitResult hitResult = getTarget().clipServerPlayer();
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult);
    }


    @LuaFunction
    public final double getClipDistance(){
        return getTarget().getClipDistance();
    }

    @LuaFunction
    public final void setPitch(double degree){
        getTarget().setPitch(degree);
    }

    @LuaFunction
    public final void reset(){
        getTarget().resetView();
    }

    @LuaFunction
    public final void setClipRange(double range){
        getTarget().setClipRange(range);
    }

    @LuaFunction
    public final void setConeAngle(double angle){
        getTarget().setConeAngle(angle);
    }

    @LuaFunction
    public final void setYaw(double degree){
        getTarget().setYaw(degree);
    }

    @LuaFunction
    public final double getPitch(){
        return getTarget().getPitch();
    }

    @LuaFunction
    public final double getYaw(){
        return getTarget().getYaw();
    }

    @LuaFunction
    public final Map<String, Object> latestShip(){
        ShipHitResult hitResult = getTarget().latestShipHitResult;
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult);
    }

    @LuaFunction
    public final Map<String, Object> latestPlayer(){
        EntityHitResult hitResult = getTarget().latestServerPlayerHitResult;
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult);
    }

    @LuaFunction
    public final Map<String, Object> latestEntity(){
        EntityHitResult hitResult = getTarget().latestEntityHitResult;
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult);
    }

    @LuaFunction
    public final Map<String, Object> latestBlock(){
        BlockHitResult hitResult = getTarget().latestBlockHitResult;
        if(hitResult == null)return null;
        return CCUtils.parse(hitResult);
    }

    @LuaFunction
    public final void clipNewShip(){
        getTarget().clipNewShip();
    }

    @LuaFunction
    public final void clipNewServerPlayer(){
        getTarget().clipNewServerPlayer();
    }

    @LuaFunction
    public final void clipNewEntity(){
        getTarget().clipNewEntity();
    }

    @LuaFunction
    public final void clipNewEntityInView(){
        getTarget().clipNewEntityInView();
    }

    @LuaFunction
    public final void clipNewBlock(){
        getTarget().clipNewBlock();
    }

    // It should not be here. Just leave it here for now.
    @LuaFunction
    public final Map<String, Object> raycast(double x_0, double y_0, double z_0,
                                             double x_1, double y_1, double z_1){


        BlockHitResult p = getTarget().clipBlock(
                new Vector3d(x_0, y_0, z_0),
                new Vector3d(x_1, y_1, z_1)
        );
        if(p == null)return null;
        return Map.of(
                "hit", Map.of(
                        "x", p.getLocation().x,
                        "y", p.getLocation().y,
                        "z", p.getLocation().z
                ),
                "direction", p.getDirection().name()
        );
    }


    @LuaFunction
    public final List<Map<String, Object>> getEntities(double radius){
        Level level = getTarget().getLevel();
        if(level == null)return List.of();
        return MinecraftUtils.getLivingEntities(
                ValkyrienSkies.toMinecraft(getTarget().getCameraPosition()),
                radius,
                level
        ).stream().map(CCUtils::parse).toList();
    }

    @LuaFunction
    public final List<Map<String, Object>> getMobs(double radius){
        Level level = getTarget().getLevel();
        if(level == null)return List.of();
        return MinecraftUtils.getMobs(
                ValkyrienSkies.toMinecraft(getTarget().getCameraPosition()),
                radius,
                level
        ).stream().map(CCUtils::parse).toList();
    }

}
