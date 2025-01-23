package com.verr1.vscontrolcraft.events;

import com.verr1.vscontrolcraft.base.ICameraAccessor;
import com.verr1.vscontrolcraft.base.Wand.ClientWand;
import com.verr1.vscontrolcraft.blocks.camera.CameraBlockEntity;
import com.verr1.vscontrolcraft.blocks.camera.LinkedCameraManager;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerTargetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ControlCraftClientEvents {

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event){
        LinkedCameraManager.tick();
        ClientWand.tick();
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event){
        float partialTicks = (float)event.getPartialTick();
        CameraBlockEntity camera = LinkedCameraManager.getLinkedCamera();
        if(camera == null)return;
        if(event.getCamera() instanceof ICameraAccessor mixinedCamera){
            mixinedCamera.controlCraft$setDetached(true);
        }
        //event.getCamera().getEntity().xo
        //event.setPitch(camera.getPitch());
        //event.setRoll(0);
        //event.setYaw(camera.getYaw());
    }



}
