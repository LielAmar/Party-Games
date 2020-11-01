package com.lielamar.partygames.listeners;

import com.lielamar.partygames.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class OnPlayerJoin implements Listener {

    private Main main;
    public OnPlayerJoin(Main main) {
        this.main = main;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent e) {
        // Making all players visible
        for(Player pl : Bukkit.getOnlinePlayers())
            pl.showPlayer(e.getPlayer());

        // Injecting to the scoreboard
        this.main.getScoreboardManager().injectPlayer(e.getPlayer());

        // If the player wasn't added to the game
        if(!main.getGame().addPlayer(e.getPlayer(), false)) {
            if(!e.getPlayer().hasPermission("partygames.spectate")) {
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.setKickMessage(ChatColor.RED + "The game has already started and we couldn't add you to it!");
                this.main.getScoreboardManager().ejectPlayer(e.getPlayer());
                return;
            }

            this.main.getScoreboardManager().getScoreboard(e.getPlayer()).setScoreboard(this.main.getGame().getStaffScoreboard());
            for(Player pl : Bukkit.getOnlinePlayers()) {
                if(pl.hasPermission("partygames.seespectators") && !this.main.getGame().containsPlayer(pl))
                    pl.hidePlayer(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
    }
}