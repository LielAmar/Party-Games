package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.validation.DoubleValidation;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AnvilSpleef extends Minigame implements Listener {

    private static int radius = 15;
    private static double minimum_y = 90.0;
    private static double max_distance_from_middle = 30.0;
    private static int amount_of_anvils = 8;
    private static int player_spleef_delay = 4;

    private List<Location> anvilLocations;

    public AnvilSpleef(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.radius")) radius = config.getInt("parameters.radius");
        if(config.contains("parameters.minimum_y")) minimum_y = config.getDouble("parameters.minimum_y");
        if(config.contains("parameters.max_distance_from_middle")) max_distance_from_middle = config.getDouble("parameters.max_distance_from_middle");
        if(config.contains("parameters.amount_of_anvils")) amount_of_anvils = config.getInt("parameters.amount_of_anvils");
        if(config.contains("parameters.player_spleef_delay")) player_spleef_delay = config.getInt("parameters.player_spleef_delay");

        anvilLocations = new ArrayList<>();

        try {
            super.validateVariables(
                    new IntValidation(radius, "[Anvil Spleef] Radius must be greater than 0", 1),
                    new DoubleValidation(max_distance_from_middle, "[Anvil Spleef] Max Distance From Middle must be greater than 0", 1),
                    new IntValidation(amount_of_anvils, "[Anvil Spleef] Amount of Anvils must be greater than 0", 1),
                    new IntValidation(player_spleef_delay, "[Anvil Spleef] Player Spleef Delay must be greater than/equals to 0", 0));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        this.startAdditionalTimer();
    }

    /**
     * An additional timer running alongside the main minigame timer
     * Calls spawnAnvils() every 20 ticks (1 second)
     */
    public void startAdditionalTimer() {
        new BukkitRunnable() {
            int i = getGameTime();

            @Override
            public void run() {
                if (i == 0) {
                    this.cancel();
                    return;
                }

                if(getGameState() == GameState.IN_GAME)
                    spawnAnvil(i);

                i--;
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 20L);
    }

    /**
     * Spawns AMOUNT_OF_ANVILS anvils in different locations
     * Spawns an anvil above every player every 4 seconds
     */
    public void spawnAnvil(int index) {
        Location anvilLoc;
        for(int i = 0; i < amount_of_anvils; i++) {
            int x = Main.rnd.nextInt(radius * 2 + 1) - radius;
            int z = Main.rnd.nextInt(radius * 2 + 1) - radius;
            anvilLoc = super.getMiddle().clone().add(x, 20, z);

            int locationSwaps = 0;
            while(this.anvilLocations.contains(anvilLoc)) {
                x = Main.rnd.nextInt(radius * 2 + 1) - radius;
                z = Main.rnd.nextInt(radius * 2 + 1) - radius;
                anvilLoc = super.getMiddle().clone().add(x, 20, z);
                locationSwaps++;
                if(locationSwaps >= 10) break;
            }

            this.anvilLocations.add(anvilLoc);
            anvilLoc.getBlock().setType(Material.ANVIL);
        }

        if(index%player_spleef_delay != 0) return;

        for(CustomPlayer pl : super.getGame().getPlayers()) {
            if(pl == null) continue;
            if(pl.isSpectator()) continue;

            anvilLoc = new Location(pl.getPlayer().getWorld(),
                    pl.getPlayer().getLocation().getX(),
                    super.getMiddle().getY()+20,
                    pl.getPlayer().getLocation().getZ());

            if(anvilLocations.contains(anvilLoc)) continue;

            this.anvilLocations.add(anvilLoc);
            anvilLoc.getBlock().setType(Material.ANVIL);
        }
    }


    @EventHandler
    public void onPlayerFallOffMap(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof AnvilSpleef)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(e.getTo().getY() < minimum_y || e.getTo().distance(super.getMiddle()) > max_distance_from_middle) {
            if(super.getGameState() != GameState.IN_GAME) {
                e.getPlayer().teleport(getMiddle());
                return;
            }

            if(super.getGame().getPlayers()[playerIndex].isSpectator()) {
                e.setTo(super.getMiddle());
                return;
            }

            super.initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
        }
    }

    @EventHandler
    public void onPlayerDamageByAnvil(EntityDamageByEntityEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof AnvilSpleef)) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        if(!(e.getEntity() instanceof Player)) return;
        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
            FallingBlock f = (FallingBlock) e.getDamager();
            if(f.getMaterial() == Material.ANVIL)
                super.initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof AnvilSpleef)) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        if(e.getEntity() instanceof FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock) e.getEntity();

            if (fallingBlock.getMaterial() == Material.ANVIL) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        e.getEntity().getLocation().clone().add(0, -1, 0).getBlock().setType(Material.AIR);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                e.getEntity().remove();
                            }
                        }.runTaskLater(getGame().getMain(), 60L);
                    }
                }.runTaskLater(super.getGame().getMain(), 1L);
            }
        }
    }
}