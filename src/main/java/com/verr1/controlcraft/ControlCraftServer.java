package com.verr1.controlcraft;


import com.verr1.controlcraft.foundation.executor.Executor;
import net.minecraft.server.MinecraftServer;

public class ControlCraftServer {
    public static MinecraftServer INSTANCE;
    // public static final DeferralExecutor SERVER_DEFERRAL_EXECUTOR = new DeferralExecutor();
    // public static final IntervalExecutor SERVER_INTERVAL_EXECUTOR = new IntervalExecutor();
    public static final Executor SERVER_EXECUTOR = new Executor();

    public static void ServerInit(){

    }
}
