package com.lielamar.partygames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class OnHungerChange implements Listener {

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
        e.setFoodLevel(20);
    }
}
