package com.lielamar.partygames.game.games;

import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.models.entities.ControllableCow;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LawnMoower extends Minigame implements Listener {

    public static Map<UUID, ControllableCow> cows = new HashMap<>();

    private static boolean constant_movement = false;

    public LawnMoower(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.constant_movement")) constant_movement = config.getBoolean("parameters.constant_movement");

        this.setupCows();
    }

    @Override
    public void extraStartParameters() {
        for(ControllableCow cow : cows.values()) {
            if(cow != null) {
                cow.getControllableEntityHandler().setCanMove(true);
                cow.getControllableEntityHandler().setConstantMovement(constant_movement);
            }
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            super.getGame().getMain().getPacketReader().eject(cp.getPlayer());
        }

        for(ControllableCow cow : cows.values())
            cow.destroy();

        cows = new HashMap<>();
    }


    /**
     * Sets up all cows & players
     */
    public void setupCows() {
        new BukkitRunnable() {

            @Override
            public void run() {
                ControllableCow cow;

                for(CustomPlayer cp : getGame().getPlayers()) {
                    if(cp == null) continue;

                    getGame().getMain().getPacketReader().inject(cp.getPlayer());

                    cow = new ControllableCow(getGame().getMain(), cp.getPlayer().getWorld()).spawnCustomEntity(cp.getPlayer().getLocation());
                    if(cp.getPlayer().isSneaking())
                        cp.getPlayer().setSneaking(false);
                    cow.getBukkitEntity().setPassenger(cp.getPlayer());
                    cows.put(cp.getPlayer().getUniqueId(), cow);
                }
            }
        }.runTaskLater(super.getGame().getMain(), 3L);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof LawnMoower)) return;

        if(super.getGameState() == GameState.GAME_END || super.getGameState() == GameState.COUNTING_DOWN) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(e.getPlayer().getLocation().clone().getBlock().getType() == Material.DOUBLE_PLANT) {
            super.getGame().getPlayers()[playerIndex].addMinigameScore(1);
            e.getPlayer().getLocation().getBlock().setType(Material.AIR);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.DIG_GRASS, 1F, 1F);
        }
    }
}