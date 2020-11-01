package com.lielamar.partygames.listeners;

import com.lielamar.partygames.Main;
import com.lielamar.partygames.listeners.custom.GameEndEvent;
import com.lielamar.partygames.listeners.custom.MinigameWinEvent;
import com.lielamar.partygames.models.CustomPlayer;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class OnPlayerWinsMinigame implements Listener {

    private Main main;
    private Random rnd;

    public OnPlayerWinsMinigame(Main main) {
        this.main = main;
        this.rnd = new Random();
    }

    @EventHandler
    public void onWin(MinigameWinEvent e) {
        if(e.getFirst() == null) return;
        Player winner = e.getFirst().getPlayer();

        runWin(winner);

        e.getMinigame().getGame().infoPlayers(ChatColor.GREEN + "" + ChatColor.BOLD + "------------------------------------------");
        e.getMinigame().getGame().infoPlayers(ChatColor.WHITE + "" + ChatColor.BOLD + "                " + e.getMinigame().getMinigameName());

        if(e.getFirst() != null) {
            e.getFirst().addScore(3);
            e.getMinigame().getGame().infoPlayers(" " + ChatColor.YELLOW + "1st  " + ChatColor.GRAY + "(" + ChatColor.YELLOW + "✯✯✯" + ChatColor.GRAY + ") - " + e.getFirst().getPlayer().getDisplayName());
        }
        if(e.getSecond() != null) {
            e.getSecond().addScore(2);
            e.getMinigame().getGame().infoPlayers(" " + ChatColor.GOLD + "2nd  " + ChatColor.GRAY + "(" + ChatColor.YELLOW + "✯✯" + ChatColor.GRAY + ") - " + e.getSecond().getPlayer().getDisplayName());
        }
        if(e.getThird() != null) {
            e.getThird().addScore(1);
            e.getMinigame().getGame().infoPlayers(" " + ChatColor.RED + "3rd  " + ChatColor.GRAY + "(" + ChatColor.YELLOW + "✯" + ChatColor.GRAY + ") - " + e.getThird().getPlayer().getDisplayName());
        }

        for(CustomPlayer cp : e.getMinigame().getGame().getPlayers()) {
            if(cp == null) continue;
            cp.getPlayer().sendMessage(" You have " + cp.getScore() + ChatColor.DARK_GRAY + " x " + ChatColor.YELLOW + "✯");
        }

        e.getMinigame().getGame().infoPlayers(ChatColor.GREEN + "" + ChatColor.BOLD + "------------------------------------------");
    }

    @EventHandler
    public void onGameWin(GameEndEvent e) {
        if(e.getFirst() == null) return;
        Player winner = e.getFirst().getPlayer();

        runWin(winner);

        e.getGame().infoPlayers(ChatColor.GREEN + "" + ChatColor.BOLD + "------------------------------------------");
        if(e.getFirst() !=  null) e.getGame().infoPlayers(" " + ChatColor.YELLOW + "1st  " + ChatColor.GRAY + "(" + ChatColor.YELLOW + "x" + e.getFirst().getScore() + "✯" + ChatColor.GRAY + ") - " + e.getFirst().getPlayer().getDisplayName());
        if(e.getSecond() !=  null) e.getGame().infoPlayers(" " + ChatColor.GOLD + "2nd  " + ChatColor.GRAY + "(" + ChatColor.GOLD + "x" + e.getSecond().getScore() + "✯" + ChatColor.GRAY + ") - " + e.getSecond().getPlayer().getDisplayName());
        if(e.getThird() !=  null) e.getGame().infoPlayers(" " + ChatColor.RED + "3rd  " + ChatColor.GRAY + "(" + ChatColor.RED + "x" + e.getThird().getScore() + "✯" + ChatColor.GRAY + ") - " + e.getThird().getPlayer().getDisplayName());
        e.getGame().infoPlayers(ChatColor.GREEN + "" + ChatColor.BOLD + "------------------------------------------");

        new BukkitRunnable() {
            @Override
            public void run() {
                for(CustomPlayer cp : e.getGame().getPlayers()) {
                    if(cp != null)
                        cp.getPlayer().kickPlayer("Game ended. Please rejoin!");
                }
                Bukkit.getServer().reload();
                // Developer Note:
                // In production, this is to be changed to Bukkit.getServer().stop();, as well as setting a script to restart the server completele.y
                // Reloading is not to be trusted and may cause issues!
            }
        }.runTaskLater(e.getGame().getMain(), 200L);
    }


    private final FireworkEffect.Type[] types = {FireworkEffect.Type.BALL, FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR, FireworkEffect.Type.BURST, FireworkEffect.Type.STAR};
    private final Color[] colors = {Color.YELLOW, Color.RED, Color.AQUA, Color.LIME, Color.BLUE, Color.PURPLE };

    public void runWin(Player winner) {
        new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    return;
                }

                winner.getWorld().playSound(winner.getLocation(), Sound.CAT_MEOW, 1, 0.2F);

                Firework fw = (Firework) winner.getWorld().spawnEntity(winner.getLocation().clone().add(rnd.nextInt(5), 0, rnd.nextInt(5)), EntityType.FIREWORK);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().with(types[rnd.nextInt(types.length)])
                        .withColor(colors[rnd.nextInt(colors.length)]).withColor(colors[rnd.nextInt(colors.length)])
                        .withColor(colors[rnd.nextInt(colors.length)]).flicker(false).trail(true).build());
                meta.setPower(0);
                fw.setFireworkMeta(meta);

                i--;
            }
        }.runTaskTimer(this.main, 0L, 20L);
    }
}
