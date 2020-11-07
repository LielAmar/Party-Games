package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.CharValidation;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.utils.GameUtils;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CannonPainters extends Minigame implements Listener {

    private static char axis = 'x';
    private static int default_wool_color = 4;
    private static Integer[] wool_colors = new Integer[] { 1, 2, 3, 5, 6, 9, 11, 14 };

    private Map<Integer, CustomPlayer> assignedColors;

    public CannonPainters(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.axis")) axis = config.getString("parameters.axis").toLowerCase().charAt(0);
        if(config.contains("parameters.default_wool_color")) default_wool_color = config.getInt("parameters.default_wool_color");
        if(config.contains("parameters.wool_colors") && config.isList("parameters.wool_colors"))
            wool_colors = config.getIntegerList("parameters.wool_colors").toArray(new Integer[0]);

        this.assignedColors = new HashMap<>();

        GameUtils.assignColorsToPlayers(super.getGame().getPlayers(), wool_colors, this.assignedColors);

        try {
            super.validateVariables(
                    new CharValidation(axis, "[Cannon Painters] Axis must be from the allowed Axes list: x/z", new Character[] { 'x', 'z' }),
                    new IntValidation(default_wool_color, "[Cannon Painters] Default Wool Color must be greater than/equals to 0 and less than/equals to 15", 0, 15),
                    new IntValidation(wool_colors.length, "[Cannon Painters] Amount of Colors must be greater than/equals to Amount Of Players", super.getGame().getPlayers().length));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        for(CustomPlayer pl : super.getGame().getPlayers()) {
            if(pl == null) continue;
            pl.getPlayer().getInventory().addItem(new ItemStack(Material.EGG, 16));
        }
    }


    /**
     * Paints a 3x3 area by the player's color
     *
     * @param cp         {@link CustomPlayer} Object of the painter
     * @param location   3x3 area middle
     */
    @SuppressWarnings("deprecation")
    public void setColorOnCanvas(CustomPlayer cp, Location location) {
        List<Block> changedBlocks = new ArrayList<>();

        int xFixer = 0;
        int zFixer = 0;

        if(location.clone().add(1, 0, 0).getBlock().getType() == Material.WOOL) xFixer = 1;
        else if(location.clone().add(-1, 0, 0).getBlock().getType() == Material.WOOL) xFixer = -1;
        else if(location.clone().add(0, 0, 1).getBlock().getType() == Material.WOOL) zFixer = 1;
        else if(location.clone().add(0, 0, -1).getBlock().getType() == Material.WOOL) zFixer = -1;

        Block block;
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++) {

                if(axis == 'x') block = location.clone().add(xFixer, j, i+zFixer).getBlock();
                else block = location.clone().add(i+xFixer, j, zFixer).getBlock();

                if(block.getType() != Material.WOOL)
                    continue;

                Wool wool = (Wool) block.getState().getData();
                if(wool.getColor() == SpigotUtils.translateIntToColor(GameUtils.getPlayerAssignedColor(cp, this.assignedColors)))
                    continue;

                int color = SpigotUtils.translateColorToInt(wool.getColor());
                if(this.assignedColors.get(color) != null)
                    this.assignedColors.get(color).takeMinigamePoints(1);

                block.setType(Material.WOOL);
                block.setData((byte) GameUtils.getPlayerAssignedColor(cp, this.assignedColors), true);

                changedBlocks.add(block);
                cp.addMinigameScore(1);
            }
        }

        new BukkitRunnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                for(Block b : changedBlocks) cp.getPlayer().sendBlockChange(b.getLocation(), Material.WOOL, (byte)default_wool_color);
            }
        }.runTaskLater(super.getGame().getMain(), 2);
    }


    @EventHandler
    public void onEggThrough(ProjectileLaunchEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof CannonPainters)) return;

        if(!(e.getEntity().getShooter() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex(((Player)e.getEntity().getShooter()));
        if(playerIndex == -1) return;

        if(super.getGameState() != GameState.IN_GAME)
            e.setCancelled(true);
        else
            ((Player) e.getEntity().getShooter()).getInventory().addItem(new ItemStack(Material.EGG));
    }

    @EventHandler
    public void onEggLand(ProjectileHitEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof CannonPainters)) return;

        if(!(e.getEntity() instanceof Egg)) return;
        if(!(e.getEntity().getShooter() instanceof Player)) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        int playerIndex = super.getGame().getPlayerIndex((Player)e.getEntity().getShooter());
        if(playerIndex == -1) return;

        setColorOnCanvas(super.getGame().getPlayers()[playerIndex], e.getEntity().getLocation().clone().add(0, 0, -1));
    }

    @EventHandler
    public void chickenSpawnEvent(EntitySpawnEvent e) {
        if(this.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof CannonPainters)) return;

        if(!(e.getEntity() instanceof Chicken)) return;

        e.setCancelled(true);
    }
}