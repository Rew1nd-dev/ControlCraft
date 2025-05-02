package com.verr1.controlcraft.content.cctweaked.delegation;

import com.verr1.controlcraft.Config;
import com.verr1.controlcraft.ControlCraft;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.peripheral.monitor.MonitorWatcher;
import dan200.computercraft.shared.util.TickScheduler;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ComputerCraftDelegation {

    private static MinecraftServer server = null;
    private static Thread ComputerCraftDelegateThread = null;
    private static final Object lock = new Object();
    private static boolean isLocked = true;


    private static final ConcurrentLinkedQueue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();

    public static void issueMainThreadTask(Runnable r){
        if(mainThreadTasks.size() < 512)mainThreadTasks.add(r);
    }

    private static void executeAllMainThreadTask(){
        while (!mainThreadTasks.isEmpty())mainThreadTasks.poll().run();
    }

    public static void tick(){
        executeAllMainThreadTask();
    }


    private static void DelegateRun() {
        if(!Config.OVERCLOCK_COMPUTERCRAFT)return;
        while (true) {
            synchronized (lock) {
                while (isLocked) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                try{
                    // ControlCraft.LOGGER.info("log from another thread!");
                    ServerContext.get(server).tick();
                    TickScheduler.tick();
                    MonitorWatcher.onTick();
                    isLocked = true;
                }catch (IllegalStateException e){
                    ControlCraft.LOGGER.info("IllegalStateException caught: " + e.getMessage());
                }


            }
        }
    }

    public static synchronized void DelegateThreadStart() {
        if (ComputerCraftDelegateThread == null || !ComputerCraftDelegateThread.isAlive()) {
            ComputerCraftDelegateThread = new Thread(ComputerCraftDelegation::DelegateRun);
            ComputerCraftDelegateThread.start();
        }
    }

    public static synchronized void DelegateThreadKill() {
        if (ComputerCraftDelegateThread != null && ComputerCraftDelegateThread.isAlive()) {
            ComputerCraftDelegateThread.interrupt();
            try {
                ComputerCraftDelegateThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ComputerCraftDelegateThread = null;
        }
    }

    public static void FreeDelegateThread() {
        synchronized (lock) {
            isLocked = false;
            lock.notify();
        }
    }

    public static void LockDelegateThread() {
        synchronized (lock) {
            isLocked = true;
        }
    }

    public static void setServer(MinecraftServer server) {
        ComputerCraftDelegation.server = server;
    }
}
