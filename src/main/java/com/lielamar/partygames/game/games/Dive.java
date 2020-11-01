package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.utils.GameUtils;
import com.lielamar.partygames.utils.Parameters;
import com.packetmanager.lielamar.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class Dive extends Minigame implements Listener {

    private static double minimum_y = 15.0;
    private static Integer[] wool_colors = new Integer[] { 1, 2, 3, 4, 5, 6, 11, 14 };

    private Map<Integer, CustomPlayer> assignedColors;

    public Dive(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.minimum_y")) minimum_y = config.getDouble("parameters.minimum_y");
        if(config.contains("parameters.wool_colors") && config.isList("parameters.wool_colors"))
            wool_colors = config.getIntegerList("parameters.wool_colors").toArray(new Integer[0]);

        this.assignedColors = new HashMap<>();

        GameUtils.assignColorsToPlayers(super.getGame().getPlayers(), wool_colors, this.assignedColors);

        try {
            super.validateVariables(
                    new IntValidation(wool_colors.length, "[Dive] Amount of Colors must be greater than/equals to Amount Of Players", super.getGame().getPlayers().length));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Paints a water block by the player's color
     *
     * @param player   Painter
     * @param block    Block to paint
     */
    @SuppressWarnings("deprecation")
    public void setColorOnWater(Player player, Block block) {
        CustomPlayer cp = super.getGame().getPlayers()[getGame().getPlayerIndex(player)];

        block.setType(Material.WOOL);
        block.setData((byte) GameUtils.getPlayerAssignedColor(cp, this.assignedColors), true);

        cp.addMinigameScore(1);
        PacketManager.sendActionbar(player, ChatColor.GREEN + "+1 score");
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Dive)) return;

        if(!(e.getEntity() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity());
        if(playerIndex == -1) return;

        if(e.getCause() == EntityDamageEvent.DamageCause.FALL)
            e.getEntity().teleport(super.getLocations()[playerIndex]);

        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Dive)) return;

        if(super.getGameState() == GameState.GAME_END) return;

        int playerIndex = super.getGame().getPlayerIndex((e.getPlayer()));
        if(playerIndex == -1) return;

        boolean preGame = super.getGameState() != GameState.IN_GAME;
        boolean blockChanged = (int)e.getTo().getX() != (int)e.getFrom().getX()
                            || (int)e.getTo().getY() != (int)e.getFrom().getY()
                            || (int)e.getTo().getZ() != (int)e.getFrom().getZ();
        if(preGame && blockChanged) {
            e.setTo(e.getFrom());
            return;
        }

        Player p = e.getPlayer();
        if(e.getTo().getBlock().getType() == Material.WATER || e.getTo().getBlock().getType() == Material.STATIONARY_WATER) {
            p.teleport(super.getLocations()[playerIndex]);
            p.playSound(p.getLocation(), Sound.SUCCESSFUL_HIT, 1F, 1F);
            setColorOnWater(p, e.getTo().getBlock());
        } else if(e.getTo().getY() < minimum_y)
            p.teleport(super.getLocations()[playerIndex]);
    }
}