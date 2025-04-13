package com.verr1.controlcraft;

import com.verr1.controlcraft.foundation.executor.DeferralExecutor;
import com.verr1.controlcraft.foundation.executor.IntervalExecutor;
import net.minecraft.server.MinecraftServer;

public class ControlCraftServer {
    public static MinecraftServer INSTANCE;
    public static final DeferralExecutor SERVER_DEFERRAL_EXECUTOR = new DeferralExecutor();
    public static final IntervalExecutor SERVER_INTERVAL_EXECUTOR = new IntervalExecutor();

    public static void ServerInit(){

    }
}
