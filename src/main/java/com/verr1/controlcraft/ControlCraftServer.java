package com.verr1.controlcraft;


import com.verr1.controlcraft.content.compact.tweak.RedstoneLinkNetworkHandlerExtension;
import com.verr1.controlcraft.foundation.executor.Executor;
import net.minecraft.server.MinecraftServer;

public class ControlCraftServer {
    public static MinecraftServer INSTANCE;
    public static RedstoneLinkNetworkHandlerExtension DECIMAL_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandlerExtension();
    public static final Executor SERVER_EXECUTOR = new Executor();

    public static void ServerInit(){

    }
}
