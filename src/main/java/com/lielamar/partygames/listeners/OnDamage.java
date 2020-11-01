package com.lielamar.partygames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class OnDamage implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent e) {
        if(!(e.getEntity() instanceof Player)) return;

        e.setCancelled(true);
    }
}
