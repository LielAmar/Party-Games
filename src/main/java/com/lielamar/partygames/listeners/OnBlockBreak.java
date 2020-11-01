package com.lielamar.partygames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class OnBlockBreak implements Listener {

    @EventHandler (priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent e) {
        if(!e.getPlayer().hasPermission("partygames.admin"))
            e.setCancelled(true);
    }
}
