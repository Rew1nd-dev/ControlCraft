package com.verr1.vscontrolcraft;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class Debug {

    public static void tick(TickEvent.ServerTickEvent event){
        ServerLevel overworldLevel = event.getServer().getLevel(ServerLevel.OVERWORLD);
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld(overworldLevel);

        if(shipWorldCore == null) return;
        var tasks = shipWorldCore.getChunkWatchTasks();

        if(tasks == null) return;
        // ControlCraft.LOGGER.info("Tasks: " + tasks.toString());
        /*
            var changes = ((ShipObjectServerWorld) shipWorldCore).getCurrentTickChanges();
            if(changes.getDeletedShipObjects().size() != 0){
                ControlCraftMod.LOGGER.info("Deleted Ship Objects: " + changes.getDeletedShipObjects().toString());
            }
        */


    }

}
