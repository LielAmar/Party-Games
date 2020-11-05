package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HighGround extends Minigame implements Listener {

    private static int ticks_per_point = 20;
    private static double minimum_y = 15.0;
    private static ItemStack item = SpigotUtils.getItem(Material.RAW_FISH, 1, ChatColor.YELLOW + "Knockback Machine");
    private static Material block_type = Material.WOOL;

    private Map<UUID, Integer> scoreHolder;

    public HighGround(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.minimum_y")) minimum_y = config.getDouble("parameters.minimum_y");
        if(config.contains("parameters.item")) item = SpigotUtils.getItem(Material.valueOf(config.getString("parameters.item")), 1, ChatColor.YELLOW + "Knockback Machine");
        if(config.contains("parameters.block_type")) block_type = Material.valueOf(config.getString("parameters.block_type"));
        if(config.contains("parameters.ticks_per_point")) ticks_per_point = config.getInt("parameters.ticks_per_point");

        this.scoreHolder = new HashMap<>();

        try {
            super.validateVariables(
                    new IntValidation(ticks_per_point, "[High Ground] Ticks Per Point must be greater than 0", 1));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        super.teleportPlayers();
        this.startAdditionalTimer();

        Map<Enchantment, Integer> enchantments = new HashMap<>();
        enchantments.put(Enchantment.KNOCKBACK, 3);
        item.addUnsafeEnchantments(enchantments);

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            cp.getPlayer().getInventory().addItem(item);
        }
    }


    /**
     * An additional timer running alongside the main minigame timer
     * Checks whether or not to add score to a player
     */
    public void startAdditionalTimer() {
        new BukkitRunnable() {
            int i = getGameTime()*20;

            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    return;
                }

                if(getGameState() == GameState.IN_GAME) {
                    for(CustomPlayer cp : getGame().getPlayers()) {
                        if(cp == null) continue;

                        if(cp.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == block_type) {
                            if(!scoreHolder.containsKey(cp.getPlayer().getUniqueId())) {
                                scoreHolder.put(cp.getPlayer().getUniqueId(), 0);
                                continue;
                            }

                            scoreHolder.put(cp.getPlayer().getUniqueId(), scoreHolder.get(cp.getPlayer().getUniqueId()) + 1);
                            if(scoreHolder.get(cp.getPlayer().getUniqueId()) >= ticks_per_point) {
                                scoreHolder.remove(cp.getPlayer().getUniqueId());
                                cp.addMinigameScore(1);
                            }
                        }
                    }
                }
                i--;
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 1L);
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof HighGround)) return;

        if(!(e.getEntity() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        e.setCancelled(super.getGameState() != GameState.IN_GAME);

        e.setDamage(0);
    }

    @EventHandler
    public void onHighGround(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof HighGround)) return;

        if(super.getGameState() == GameState.GAME_END) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(e.getPlayer().getLocation().getY() < minimum_y)
            e.getPlayer().teleport(getLocations()[playerIndex]);
    }
}