package com.lielamar.partygames.listeners;

import com.lielamar.partygames.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class OnPlayerDeath implements Listener {

    private Main main;

    public OnPlayerDeath(Main main) {
        this.main = main;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onRespawn(PlayerRespawnEvent e) {
        if(main == null) return;
        if(main.getGame() == null) return;
        if(main.getGame().getCurrentGame() == null) return;

        if(main.getGame().containsPlayer(e.getPlayer()))
            e.getPlayer().teleport(main.getGame().getCurrentGame().getMiddle());
    }
}