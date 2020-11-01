package com.lielamar.partygames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class OnItemPickup implements Listener {

    @EventHandler
    public void onHungerChange(PlayerPickupItemEvent e) {
        if(!e.getPlayer().hasPermission("partygames.admin"))
            e.setCancelled(true);
    }
}
