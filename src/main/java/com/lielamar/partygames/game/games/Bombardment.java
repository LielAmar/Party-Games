package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.lielsutils.validation.DoubleValidation;
import com.lielamar.partygames.PartyGames;
import com.lielamar.partygames.game.*;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.modules.objects.Bomb;
import com.lielamar.partygames.game.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Bombardment extends Minigame implements Listener {

    private static double max_distance_from_middle = 30.0;
    private static Material bomb_type = Material.COAL_BLOCK;

    private int currentWave;
    private Location[] cannons;
    private List<Location> shootLocations;

    public Bombardment(Game game, GameType gameType) {
        super(game, gameType);
        Bukkit.getPluginManager().registerEvents(this, getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        if(config.contains("parameters.max_distance_from_middle")) max_distance_from_middle = config.getDouble("parameters.max_distance_from_middle");
        if(config.contains("parameters.bomb_type")) bomb_type = Material.valueOf(config.getString("parameters.bomb_type"));

        this.currentWave = 1;
        this.cannons = SpigotUtils.fetchLocations(config, "cannons");

        this.setupShootLocations();

        try {
            super.validateVariables(
                    new DoubleValidation(max_distance_from_middle, "[Bombardment] Max Distance From Middle must be greater than 0", 1));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        this.startAdditionalTimer();
    }

    @Override
    public void updateMinigameScoreboard(Pair<?, ?>... pairs) {
        super.updateMinigameScoreboard(new Pair<>("%wave%", currentWave + ""));
    }


    /**
     * An additional timer running alongside the main minigame timer
     * Spawns bombs in X speed (depending on time passed)
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

                if(getGameType().getGameDuration()-i == 15) currentWave = 2;
                else if(getGameType().getGameDuration()-i == 30) currentWave = 3;
                else if(getGameType().getGameDuration()-i == 45) currentWave = 4;
                else if(getGameType().getGameDuration()-i == 60) currentWave = 5;
                else if(getGameType().getGameDuration()-i == 75) currentWave = 6;
                else if(getGameType().getGameDuration()-1 == 90) currentWave = 7;

                if(i == getGameType().getGameDuration())
                    new Bomb(getGame(), cannons[0], getMiddle().clone().add(0, -4, 0), Material.COAL_BLOCK, currentWave);

                if(getGameState() == GameState.IN_GAME)
                    handleBombShooting(getGameType().getGameDuration()-i);

                i--;
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 20L);
    }

    /**
     * Sets up all locations that the ship can fire towards by StartPoint, EndPoint and distance between each point
     */
    public void setupShootLocations() {
        this.shootLocations = new ArrayList<>();

        double minX = config.getInt("shoot.minX");
        double maxX = config.getInt("shoot.maxX");
        double y = config.getInt("shoot.Y");
        double minZ = config.getInt("shoot.minZ");
        double maxZ = config.getInt("shoot.maxZ");
        double distance = config.getInt("shoot.distance");
        World world = super.getMiddle().getWorld();

        for(double x = 0; Math.abs(x) <= Math.abs(minX-maxX); x += (minX > maxX) ? -distance : distance) {
            for(double z = 0; Math.abs(z) <= Math.abs(minZ-maxZ); z += (minZ > maxZ) ? -distance : distance)
                this.shootLocations.add(new Location(world, minX+x, y-2 ,minZ+z));
        }
    }

    /**
     * Handles bomb shooting - Passing amount of bombs to shoot (depending on the phase)
     *
     * @param index   Time passed since beginning of the minigame
     */
    public void handleBombShooting(int index) {
        if(currentWave == 1) {
            if(index % 2 == 0)
                shootBomb(1);
        } else
            shootBomb(currentWave-1);
    }

    /**
     * Shoots bombs
     *
     * @param amount   Amount of bombs to shoot
     */
    public void shootBomb(int amount) {
        new BukkitRunnable() {
            int index = 0;
            @Override
            public void run() {
                if(index == amount) {
                    this.cancel();
                    return;
                }

                int bound = PartyGames.rnd.nextInt(cannons.length);
                Location from = cannons[bound];
                Location to = shootLocations.get(PartyGames.rnd.nextInt(shootLocations.size()));

                new Bomb(getGame(), from, to, bomb_type, currentWave);

                index++;
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 2L);
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Bombardment)) return;

        e.setCancelled(true);

        if(super.getGameState() != GameState.IN_GAME) return;

        if(!(e.getEntity() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
            super.initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
    }

    @EventHandler
    public void onWaterTouch(PlayerMoveEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Bombardment)) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        Player p = e.getPlayer();
        boolean isDisqualified = p.getLocation().getBlock().getType() == Material.WATER
                              || p.getLocation().getBlock().getType() == Material.STATIONARY_WATER
                              || e.getTo().distance(super.getMiddle()) > max_distance_from_middle;

        if(isDisqualified) {
            if(super.getGame().getPlayers()[playerIndex].isSpectator())
                e.setTo(super.getMiddle());
            else
                super.initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
        }
    }
}