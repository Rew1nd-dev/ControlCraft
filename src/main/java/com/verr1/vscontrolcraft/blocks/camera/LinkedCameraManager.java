package com.verr1.vscontrolcraft.blocks.camera;

import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class LinkedCameraManager {
    private static BlockPos LinkCameraPos;



    private static boolean isLinked = false;

    public static CameraBlockEntity getLinkedCamera(){
        if(LinkCameraPos == null)return null;
        if(Minecraft.getInstance().level == null)return null;
        BlockEntity optional = Minecraft
                .getInstance()
                .level
                .getBlockEntity(LinkCameraPos);

        if(optional == null){
            return null;
        }

        return optional instanceof CameraBlockEntity camera
                ? camera : null;
    }

    public static boolean isIsLinked() {
        return isLinked;
    }

    public static BlockPos getLinkCameraPos(){
        return LinkCameraPos;
    }

    public static void link (BlockPos cameraPos){
        LinkCameraPos = cameraPos;
        isLinked = true;
        Minecraft.getInstance().options.bobView().set(false);
        if(Minecraft.getInstance().level == null)return;
        //Minecraft.getInstance().level.getChunkSource().updateViewCenter(cameraPos.getX(), cameraPos.getZ());
    }

    public static void deLink(){
        disconnect();
        LinkCameraPos = null;
        isLinked = false;
        Minecraft.getInstance().options.bobView().set(true);
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        Minecraft.getInstance().levelRenderer.allChanged();
    }

    public static void disconnect(){
        if(LinkCameraPos == null)return;
        var p = new BlockBoundServerPacket.builder(LinkCameraPos, BlockBoundPacketType.EXTEND_0)
                .build();
        AllPackets.getChannel().sendToServer(p);
    }


    public static void tick(){
        CameraBlockEntity camera = getLinkedCamera();
        if(camera == null && isLinked){
            deLink();
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)return;

        if(camera != null && isLinked){
            camera.setPitch(player.getViewXRot(1));
            camera.setYaw(player.getViewYRot(1));
            camera.syncServer(player.getName().getString());
            camera.outlineViewClip();
            camera.outlineEntityClip();
            // camera.outlineClipRay();
            camera.outlineShipClip();
        }


        if(isLinked){
            Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        if(player.input.jumping && isLinked){
            deLink();
        }

    }

}
