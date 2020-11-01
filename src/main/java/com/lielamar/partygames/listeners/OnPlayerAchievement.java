package com.lielamar.partygames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;

public class OnPlayerAchievement implements Listener {

    @EventHandler
    public void onAchievement(PlayerAchievementAwardedEvent e) {
        e.setCancelled(true);
    }
}
