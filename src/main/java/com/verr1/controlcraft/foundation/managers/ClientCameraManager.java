package com.verr1.controlcraft.foundation.managers;

import com.verr1.controlcraft.content.blocks.camera.CameraBlockEntity;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ClientCameraManager {
    private static BlockPos LinkCameraPos;

    public static @Nullable CameraBlockEntity getLinkedCamera(){
        return Optional
                .ofNullable(LinkCameraPos)
                .flatMap(pos -> Optional
                                .ofNullable(Minecraft.getInstance().level)
                                .flatMap(level -> Optional
                                                    .ofNullable(level.getBlockEntity(pos))))
                .filter(CameraBlockEntity.class::isInstance)
                .map(CameraBlockEntity.class::cast)
                .orElse(null);
    }

    public static boolean isLinked() {
        return LinkCameraPos != null;
    }

    public static @Nullable BlockPos getLinkCameraPos(){
        return LinkCameraPos;
    }

    public static void link(BlockPos cameraPos){
        LinkCameraPos = cameraPos;
        Minecraft.getInstance().options.bobView().set(false);
        if(Minecraft.getInstance().level == null)return;
        //Minecraft.getInstance().level.getChunkSource().updateViewCenter(cameraPos.getX(), cameraPos.getZ());
    }

    public static void deLink(){
        disconnectServerCamera();
        LinkCameraPos = null;
        Minecraft.getInstance().options.bobView().set(true);
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        Minecraft.getInstance().levelRenderer.allChanged();
    }

    public static void disconnectServerCamera(){
        if(LinkCameraPos == null)return;
        var p = new BlockBoundServerPacket.builder(LinkCameraPos, RegisteredPacketType.EXTEND_0)
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
    }


    public static void tick(){
        CameraBlockEntity camera = getLinkedCamera();
        if(camera == null && isLinked()){
            deLink();
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)return;

        if(camera != null && isLinked()){
            camera.setPitch(player.getViewXRot(1));
            camera.setYaw(player.getViewYRot(1));
            camera.syncServer(player.getName().getString());
            // camera.outlineViewClip();
            // camera.outlineEntityClip();
            camera.outlineEntityInView();
            camera.outlineClipRay();
            camera.outlineShipClip();
        }


        if(isLinked()){
            Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        if(player.input.jumping && isLinked()){
            deLink();
        }

    }

}
