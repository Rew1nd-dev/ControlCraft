package com.verr1.vscontrolcraft.blocks.camera;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaterniond;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class CameraEntity extends LocalPlayer {
    @Nullable
    private static CameraEntity camera;
    @Nullable private static Entity originalCameraEntity;

    private Quaterniond los = new Quaterniond(); // line of sight

    private static boolean originalCameraWasPlayer;

    private CameraEntity(Minecraft mc, ClientLevel world,
                         ClientPacketListener netHandler, StatsCounter stats,
                         ClientRecipeBook recipeBook)
    {
        super(mc, world, netHandler, stats, recipeBook, false, false);
    }

    @Override
    public boolean isSpectator()
    {
        return true;
    }

}
