package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.game.*;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.game.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class FrozenFloor extends Minigame implements Listener {

    private static int radius = 18;
    private static double minimum_y = 90.0;

    private List<Block> blocksToRemove;
    private int currentRadiusToRemove;

    public FrozenFloor(Game game, GameType gameType) {
        super(game, gameType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        if(config.contains("parameters.radius")) radius = config.getInt("parameters.radius");
        if(config.contains("parameters.minimum_y")) minimum_y = config.getDouble("parameters.minimum_y");

        this.blocksToRemove = new ArrayList<>();
        this.currentRadiusToRemove = radius;

        try {
            super.validateVariables(
                            new IntValidation(radius, "[Frozen Floor] Radius must be greater than 0", 1));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        this.startAdditionalTimer();

        for(CustomPlayer pl : super.getGame().getPlayers()) {
            if(pl == null) continue;
            pl.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 16));
        }
    }

    
    /**
     * An additional timer running alongside the main minigame timer
     * Calls removeFloor() every 20 ticks (1 second)
     */
    public void startAdditionalTimer() {
        new BukkitRunnable() {
            int i = getGameType().getGameDuration();

            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    return;
                }

                if(getGameState() == GameState.IN_GAME)
                    removeFloor();

                i--;
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 20L);
    }

    /**
     * Removes the 3 furthest blocks from the middle
     */
    public void removeFloor() {
        double x, z;

        for(double i = 0.0; i < 360.0; i += 1) {
            double angle = i * Math.PI / 180;
            x = currentRadiusToRemove * Math.cos(angle);
            z = currentRadiusToRemove * Math.sin(angle);

            Block block = super.getMiddle().clone().add(x, -1, z).getBlock();
            if (block.getType() == Material.AIR) continue;

            blocksToRemove.add(block);

            if(blocksToRemove.size() >= 10) {
                for(Block b : blocksToRemove) {
                    b.getLocation().clone().add(0, -1, 0).getBlock().setType(Material.AIR);
                    b.setType(Material.AIR);
                }
                blocksToRemove.clear();
                return;
            }
        }

        currentRadiusToRemove--;
    }


    @EventHandler
    public void onPlayerFallOffMap(PlayerMoveEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof FrozenFloor)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(e.getTo() == null || super.getMiddle() == null) return;

        if(e.getTo().getY() < minimum_y) {
            if(super.getGameState() == GameState.IN_GAME)
                super.initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
            else
                e.getPlayer().teleport(super.getLocations()[playerIndex]);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof FrozenFloor)) return;

        e.setCancelled(true);

        if(super.getGameState() != GameState.IN_GAME) return;
        if(!(e.getEntity() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(e.getDamager() instanceof Player) {
            playerIndex = super.getGame().getPlayerIndex((Player)e.getDamager());
            if(playerIndex == -1) return;

            e.setCancelled(super.getGame().getPlayers()[playerIndex].isSpectator());
        } else if(e.getDamager() instanceof Snowball)
            e.setCancelled(false);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof FrozenFloor)) return;

        e.setCancelled(true);

        if(super.getGameState() != GameState.IN_GAME) return;

        if(!(e.getEntity() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        e.setCancelled(false);
        e.setDamage(0);
    }

    @EventHandler
    public void onSnowballThrough(ProjectileLaunchEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof FrozenFloor)) return;

        if(!(e.getEntity().getShooter() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex(((Player)e.getEntity().getShooter()));
        if(playerIndex == -1) return;

        if(super.getGameState() == GameState.IN_GAME)
            ((Player) e.getEntity().getShooter()).getInventory().addItem(new ItemStack(Material.SNOW_BALL));
        else
            e.setCancelled(true);
    }
}