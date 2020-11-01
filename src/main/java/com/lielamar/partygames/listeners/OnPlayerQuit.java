package com.lielamar.partygames.listeners;

import com.lielamar.partygames.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnPlayerQuit implements Listener {

    private Main main;
    public OnPlayerQuit(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        this.main.getGame().removePlayer(e.getPlayer());
        this.main.getScoreboardManager().ejectPlayer(e.getPlayer());
    }
}
