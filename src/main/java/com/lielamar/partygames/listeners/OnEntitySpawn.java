package com.lielamar.partygames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class OnEntitySpawn implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEntitySpawn(CreatureSpawnEvent e) {
        if(e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        e.setCancelled(true);
    }
}
