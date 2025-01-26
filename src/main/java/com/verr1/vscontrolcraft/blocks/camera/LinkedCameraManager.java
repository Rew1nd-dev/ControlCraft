package com.verr1.vscontrolcraft.blocks.camera;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class LinkedCameraManager {
    private static BlockPos LinkCameraPos;
    private static boolean isLinked = false;

    public static CameraBlockEntity getLinkedCamera(){
        if(LinkCameraPos == null)return null;
        if(Minecraft.getInstance().level == null)return null;
        return Minecraft
                .getInstance()
                .level
                .getExistingBlockEntity(LinkCameraPos) instanceof CameraBlockEntity
                ?
                (CameraBlockEntity)Minecraft.getInstance().level.getExistingBlockEntity(LinkCameraPos)
                :
                null;
    }

    public static BlockPos getLinkCameraPos(){
        return LinkCameraPos;
    }

    public static void link(BlockPos cameraPos){
        LinkCameraPos = cameraPos;
        isLinked = true;
        Minecraft.getInstance().options.bobView().set(false);
    }

    public static void deLink(){
        LinkCameraPos = null;
        isLinked = false;
        Minecraft.getInstance().options.bobView().set(true);
        //Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
    }



    public static void tick(){
        CameraBlockEntity camera = getLinkedCamera();
        if(camera == null){
            deLink();
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)return;

        if(isLinked){
            Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        if(player.input.jumping){
            deLink();
        }

    }

}
