package com.lielamar.partygames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OnInventoryClick implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getClickedInventory() == null || e.getInventory() == null || e.getCurrentItem() == null) return;

        if(!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player)e.getWhoClicked();

        if(e.getClickedInventory() == p.getInventory()) {
            if(!p.hasPermission("partygames.admin"))
                e.setCancelled(true);
        }
    }
}
