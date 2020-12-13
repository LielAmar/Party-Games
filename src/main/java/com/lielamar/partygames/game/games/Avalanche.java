package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.MathUtils;
import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.PartyGames;
import com.lielamar.partygames.game.*;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.game.GameType;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Avalanche extends Minigame implements Listener {

    private static int radius = 11;
    private static int amount_of_waves = 15;
    private static int wave_countdown = 5;
    private static int pvp_wave = 10;

    private int currentWave;
    private List<Location> safePoints;
    private BukkitTask spawningSnowballsTask;

    public Avalanche(Game game, GameType gameType) {
        super(game, gameType);
        Bukkit.getPluginManager().registerEvents(this, getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        if(config.contains("parameters.radius")) radius = config.getInt("parameters.radius");
        if(config.contains("parameters.amount_of_waves")) amount_of_waves = config.getInt("parameters.amount_of_waves");
        if(config.contains("parameters.wave_countdown")) wave_countdown = config.getInt("parameters.wave_countdown");
        if(config.contains("parameters.pvp_wave")) pvp_wave = config.getInt("parameters.pvp_wave");

        this.currentWave = 1;
        this.safePoints = new ArrayList<>();

        try {
            super.validateVariables(
                            new IntValidation(radius, "[Avalanche] Radius must be greater than 0", 1),
                            new IntValidation(amount_of_waves, "[Avalanche] Amount of Waves must be greater than 0", 1),
                            new IntValidation(wave_countdown, "[Avalanche] Wave Countdown must be greater than 0", 1),
                            new IntValidation(pvp_wave, "[Avalanche] PvP Wave must be greater than/equals to 0", 0));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runMinigame() {
        if(super.startMinigameTask != null) super.startMinigameTask.cancel();

        super.gameState = GameState.IN_GAME;
        super.extraStartParameters();

        this.sendWaveInfo();       // First Wave
        this.spawnSafePoints();    // First Safe Points

        super.runMinigameTask = new BukkitRunnable() {
            int i = getGameType().getGameDuration();
            int currentWaveCountdown = wave_countdown;

            @Override
            public void run() {
                if (i == 0) {
                    if(currentWave == amount_of_waves) {
                        this.cancel();
                        stopMinigame();
                        runMinigameTask = null;
                        return;
                    }

                    i = getGameType().getGameDuration();

                    currentWave++;
                    currentWaveCountdown = wave_countdown - (currentWave/5);

                    if(currentWave == pvp_wave)
                        getGame().infoPlayers(ChatColor.YELLOW + "You are no longer invulnerable!");

                    spawningSnowballsTask.cancel();
                    spawningSnowballsTask = null;

                    sendWaveInfo();
                    destroySafePoints();
                    spawnSafePoints();
                }

                updateMinigameScoreboard();
                setElapsedTime(getElapsedTime()+1);

                if(currentWaveCountdown != 0) {
                    getGame().bossbarPlayers(ChatColor.GOLD + "" + ChatColor.BOLD + "Wave Start: " + ChatColor.RED + currentWaveCountdown);
                    getGame().playSound(Sound.WOOD_CLICK, 1F, 1F);
                    currentWaveCountdown--;
                } else {
                    getGame().bossbarPlayers(ChatColor.GOLD + "" + ChatColor.BOLD + "Wave End: " + ChatColor.RED + i);

                    spawnAvalancheSnowballs();
                    i--;
                }
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 20L);
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        this.destroySafePoints();
        this.spawningSnowballsTask.cancel();
        this.spawningSnowballsTask = null;
    }

    @Override
    public void updateMinigameScoreboard(Pair<?, ?>... pairs) {
        super.updateMinigameScoreboard(new Pair<>("%wave%", currentWave + ""));
    }


    /**
     * Spawns ~24 snowballs every 1 tick for 20 ticks
     */
    public void spawnAvalancheSnowballs() {
        if(spawningSnowballsTask != null) return;

        this.spawningSnowballsTask = new BukkitRunnable() {
            int i = 20;

            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    return;
                }

                boolean spawn;

                for(int j = 0; j < 24; j++) {
                    spawn = true;
                    double x = PartyGames.rnd.nextInt(radius * 2 + 1) - radius + ((double)PartyGames.rnd.nextInt(5)/10);
                    double z = PartyGames.rnd.nextInt(radius * 2 + 1) - radius + ((double) PartyGames.rnd.nextInt(5)/10);

                    // Checks if the random location is above a safe point. If it is, we skip it
                    for(Location safePoint : safePoints) {
                        if(MathUtils.XZDistance(x, safePoint.getX(), z, safePoint.getZ()) < 1.5)
                            spawn = false;
                    }

                    if(spawn)
                        getMiddle().getWorld().spawnEntity(getMiddle().clone().add(x, 15 + ((double)PartyGames.rnd.nextInt(5)/10), z), EntityType.SNOWBALL).getVelocity().setX(0).setZ(0);
                }
                i--;
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 1L);
    }

    /**
     * Spawns an X amount of safe points (depending on wave)
     */
    public void spawnSafePoints() {
        Location safePoint;
        int counter = 0;

        while(counter < this.matchSafePoints(currentWave)) {
            int x = PartyGames.rnd.nextInt(radius * 2 + 1) - radius;
            int z = PartyGames.rnd.nextInt(radius * 2 + 1) - radius;
            safePoint = super.getMiddle().clone().add(x, 3, z);

            if(!safePoints.contains(safePoint)) {
                safePoint.getBlock().setType(Material.WOOD_STEP, false);
                safePoints.add(safePoint);
                counter++;
            }
        }
    }

    /**
     * @param wave   Wave to match safe points
     * @return       Amount of safe points for wave
     */
    public int matchSafePoints(int wave) {
        int safePointsAmount = (amount_of_waves-wave)/4;
        if(safePointsAmount == 0) safePointsAmount++;
        return safePointsAmount;
    }

    /**
     * Destroys all safe points
     */
    public void destroySafePoints() {
        for(Location loc : safePoints) loc.getBlock().setType(Material.AIR);

        this.safePoints = new ArrayList<>();
    }

    /**
     * Send a message with the Wave information to all game players
     */
    public void sendWaveInfo() {
        int currentWaveCountdown = wave_countdown - (currentWave/5);
        super.getGame().infoPlayers(ChatColor.YELLOW + "Wave " + ChatColor.RED + currentWave + ChatColor.YELLOW + " will begin in " + ChatColor.RED + currentWaveCountdown +
                ChatColor.YELLOW + " seconds with " + ChatColor.RED + matchSafePoints(currentWave) + ChatColor.YELLOW + " safe points! Find Cover!");
    }


    @EventHandler
    public void onPlayerDamageBySnowball(EntityDamageByEntityEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Avalanche)) return;

        if(super.getGameState() != GameState.IN_GAME) {
            e.setCancelled(true);
            return;
        }

        if(!(e.getEntity() instanceof Player)) return;
        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) {
            e.setCancelled(true);
            return;
        }

        if(e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            Projectile proj = (Projectile) e.getDamager();
            if(proj.getType() == EntityType.SNOWBALL)
                super.initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Avalanche)) return;

        if(!(e.getEntity() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) {
            e.setCancelled(true);
            return;
        }

        if(super.getGameState() != GameState.IN_GAME) return;
        if(e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) return;

        if(currentWave < pvp_wave)
            e.setCancelled(true);
        else {
            e.setDamage(0);
            e.setCancelled(false);
        }
    }
}