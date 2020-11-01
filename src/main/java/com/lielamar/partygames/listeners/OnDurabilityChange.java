package com.lielamar.partygames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class OnDurabilityChange implements Listener {

    @EventHandler
    public void onDurabilityChange(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }
}
