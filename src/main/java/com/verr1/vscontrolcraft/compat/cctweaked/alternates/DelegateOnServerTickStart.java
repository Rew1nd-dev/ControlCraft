package com.verr1.vscontrolcraft.compat.cctweaked.alternates;

import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.util.TickScheduler;
import net.minecraft.server.MinecraftServer;

public class DelegateOnServerTickStart {

    private static MinecraftServer server = null;

    public static void onServerPhysicsTickStart(){
        ServerContext.get(server).tick();
        TickScheduler.tick();
    }



    public static void setServer(MinecraftServer server){
        DelegateOnServerTickStart.server = server;
    }

}
