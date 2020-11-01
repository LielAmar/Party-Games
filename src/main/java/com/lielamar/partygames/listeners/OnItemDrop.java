package com.lielamar.partygames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class OnItemDrop implements Listener {

    @EventHandler
    public void onHungerChange(PlayerDropItemEvent e) {
        if(!e.getPlayer().hasPermission("partygames.admin"))
            e.setCancelled(true);
    }
}
