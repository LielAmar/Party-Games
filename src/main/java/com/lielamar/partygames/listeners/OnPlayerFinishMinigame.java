package com.lielamar.partygames.listeners;

import com.lielamar.partygames.events.PlayerFinishMinigameEvent;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnPlayerFinishMinigame implements Listener {

    @EventHandler
    public void onFinishMinigame(PlayerFinishMinigameEvent e) {
        e.getPlayer().getPlayer().playSound(e.getPlayer().getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 0.7F);


    }
}
