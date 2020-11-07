package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.validation.DoubleValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LabEscape extends Minigame implements Listener {

    private static double max_distance_from_middle = 30.0;
    private static int top_y = 89;
    private static int bottom_y = 11;
    private static List<Material> block_types = new ArrayList<>();

    public LabEscape(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.max_distance_from_middle")) max_distance_from_middle = config.getDouble("parameters.max_distance_from_middle");
        if(config.contains("parameters.top_y")) top_y = config.getInt("parameters.top_y");
        if(config.contains("parameters.bottom_y")) bottom_y = config.getInt("parameters.bottom_y");
        if(config.contains("parameters.block_types") && config.isList("parameters.block_types")) {
            for(String s : config.getStringList("parameters.block_types"))
                block_types.add(Material.valueOf(s));
        } else {
            block_types.add(Material.DIRT);
            block_types.add(Material.STONE);
            block_types.add(Material.WOOD);
        }

        this.setupPipes();

        try {
            super.validateVariables(
                    new DoubleValidation(max_distance_from_middle, "[Lab Escape] Max Distance From Middle must be greater than 0", 1));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupMinigame() {
        super.setupMinigame();

        for(int i = 0; i < super.getGame().getPlayers().length; i++) {
            if(super.getGame().getPlayers()[i] == null) continue;
            super.getGame().getPlayers()[i].getPlayer().getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
            super.getGame().getPlayers()[i].getPlayer().getInventory().addItem(new ItemStack(Material.IRON_AXE));
            super.getGame().getPlayers()[i].getPlayer().getInventory().addItem(new ItemStack(Material.IRON_SPADE));

            for(CustomPlayer cp : super.getGame().getPlayers()) {
                if(cp == null) continue;
                super.getGame().getPlayers()[i].getPlayer().showPlayer(cp.getPlayer());
            }
        }
    }


    /**
     * Sets up all pipes (wall, checkpoint, endpoint)
     */
    public void setupPipes() {
        Material[] blocks = new Material[Math.abs(top_y-bottom_y)];
        for(int i = 0; i < blocks.length; i++)
            blocks[i] = block_types.get(Main.rnd.nextInt(block_types.size()));

        Location tmpLocation;
        for(Location loc : super.getLocations()) {
            tmpLocation = new Location(loc.getWorld(), loc.getX(), bottom_y, loc.getZ());
            for(Material block : blocks)
                tmpLocation.add(0, 1, 0).getBlock().setType(block);
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof LabEscape)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) {
            if(e.getTo().distance(super.getMiddle()) > max_distance_from_middle)
                e.getPlayer().teleport(super.getMiddle());
            return;
        }

        if(super.getGameState() == GameState.IN_GAME && e.getPlayer().getLocation().getY() < bottom_y) {
            super.finishPlayer(super.getGame().getPlayers()[playerIndex], e.getPlayer().getDisplayName() + "" + ChatColor.YELLOW + " has finished the cake!");
            e.getPlayer().getInventory().clear();
            e.getPlayer().updateInventory();
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof LabEscape)) return;

        e.setCancelled(true);

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(!block_types.contains(e.getBlock().getType()) || super.getGameState() != GameState.IN_GAME)
            return;

        e.getBlock().setType(Material.AIR);
        super.getGame().getPlayers()[playerIndex].addMinigameScore(1);
    }
}