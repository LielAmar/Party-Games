package com.lielamar.partygames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class OnBlockPlace implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if(!e.getPlayer().hasPermission("partygames.admin"))
            e.setCancelled(true);
    }
}
