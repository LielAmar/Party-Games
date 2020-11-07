package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.modules.objects.Canvas;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JigsawRush extends Minigame implements Listener {

    private static List<Material> block_types = new ArrayList<>();

    private Material[][] canvas;
    private Map<CustomPlayer, Canvas> assignedCanvases;

    public JigsawRush(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.block_types") && config.isList("parameters.block_types")) {
            for(String s : config.getStringList("parameters.block_types"))
                block_types.add(Material.valueOf(s));
        } else {
            block_types.add(Material.DIRT);
            block_types.add(Material.STONE);
            block_types.add(Material.COBBLESTONE);
            block_types.add(Material.LOG);
            block_types.add(Material.WOOD);
            block_types.add(Material.SMOOTH_BRICK);
            block_types.add(Material.GOLD_BLOCK);
            block_types.add(Material.NETHERRACK);
            block_types.add(Material.ENDER_STONE);
        }

        try {
            super.validateVariables(
                    new IntValidation(block_types.size(), "[Jigsaw Rush] Amount of Blocks must be equals to 9", 9, 9));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupMinigame() {
        super.setupMinigame();

        Location[] canvases = SpigotUtils.fetchLocations(super.getGame().getMain().getFileManager()
                .getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig(), "canvases");
        this.assignedCanvases = new HashMap<>();

        if(canvases.length < super.getGame().getPlayers().length) {
            super.destroyMinigame();
            return;
        }

        for(int i = 0; i < super.getGame().getPlayers().length; i++) {
            if(super.getGame().getPlayers()[i] == null) continue;
            assignedCanvases.put(super.getGame().getPlayers()[i], new Canvas(canvases[i]));
        }
    }

    @Override
    public void extraStartParameters() {
        this.setupBigCanvases();

        for(int i = 0; i < super.getGame().getPlayers().length; i++) {
            if(super.getGame().getPlayers()[i] == null) continue;

            for(Material type : block_types)
                super.getGame().getPlayers()[i].getPlayer().getInventory().addItem(new ItemStack(type, 64));
        }
    }


    /**
     * Sets up all big canvases - Randomising the blocks
     */
    public void setupBigCanvases() {
        int amount_of_big_canvases = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig().getConfigurationSection("bigcanvases").getKeys(false).size();
        this.canvas = new Material[(int)Math.sqrt(block_types.size())][(int)Math.sqrt(block_types.size())];

        Material randomMaterial;

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                randomMaterial = block_types.get(Main.rnd.nextInt(block_types.size()));
                while(bigCanvasContains(randomMaterial))
                    randomMaterial = block_types.get(Main.rnd.nextInt(block_types.size()));
                this.canvas[i][j] = randomMaterial;
            }
        }

        Location startLoc;
        Location endLoc;
        char axis;

        for(int i = 0; i < amount_of_big_canvases; i++) {
            startLoc = SpigotUtils.fetchLocation(super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig(), "bigcanvases." + i + ".start");
            endLoc = SpigotUtils.fetchLocation(super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig(), "bigcanvases." + i + ".end");

            if(startLoc.getX() != endLoc.getX()) axis = 'x';
            else if(startLoc.getZ() != endLoc.getZ()) axis = 'z';
            else continue;

            loadBigCanvas(startLoc, endLoc, axis);
        }
    }

    /**
     * Loads a canvas
     *
     * @param start   Start location of the canvas
     * @param end     End location of the canvas
     * @param axis    Axis of the canvas
     */
    public void loadBigCanvas(Location start, Location end, char axis) {
        double canvas_block_size = 3;

        if(axis == 'x') {
            for(int x = 0; Math.abs(x) <= Math.abs(start.getX()-end.getX()); x+= (start.getX() > end.getX()) ? -1 : 1) {
                for(int y = 0; Math.abs(y) <= Math.abs(start.getY()-end.getY()); y+= (start.getY() > end.getY()) ? -1 : 1)
                    start.clone().add(x, y, 0).getBlock().setType(this.canvas[(int) (Math.abs(x) / canvas_block_size)][(int) (Math.abs(y) / canvas_block_size)]);
            }
        } else if(axis == 'z') {
            for(int z = 0; Math.abs(z) <= Math.abs(start.getZ()-end.getZ()); z+= (start.getZ() > end.getZ()) ? -1 : 1) {
                for(int y = 0; Math.abs(y) <= Math.abs(start.getY()-end.getY()); y+= (start.getY() > end.getY()) ? -1 : 1)
                    start.clone().add(0, y, z).getBlock().setType(this.canvas[(int) (Math.abs(z) / canvas_block_size)][(int) (Math.abs(y) / canvas_block_size)]);
            }
        }
    }

    /**
     * Checks if a big canvas contains the material provided
     *
     * @param material   Material to check
     * @return           Whether or not the canvas has the given material inside
     */
    public boolean bigCanvasContains(Material material) {
        for(Material[] materials : this.canvas) {
            for(int j = 0; j < this.canvas.length; j++)
                if(materials[j] == material) return true;
        }
        return false;
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof JigsawRush)) return;

        e.setCancelled(true);

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        CustomPlayer player = super.getGame().getPlayers()[playerIndex];
        if(player.isSpectator()) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        if(e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() == Material.AIR || !e.getPlayer().getItemInHand().getType().isBlock()) return;

        if(e.getClickedBlock() == null) return;

        int iRelationship = this.assignedCanvases.get(player).getIRelationship(e.getClickedBlock().getLocation());
        int jRelationship = this.assignedCanvases.get(player).getJRelationship(e.getClickedBlock().getLocation());

        if(iRelationship == -1 || jRelationship == -1) return; // If the clicked block is not next to the middle block.

        e.getClickedBlock().setType(e.getPlayer().getItemInHand().getType());
        this.assignedCanvases.get(player).getCanvas()[iRelationship][jRelationship] = e.getClickedBlock().getType();

        // If the player finished the pattern
        if(this.assignedCanvases.get(player).isSimilar(this.canvas)) {
            finishPlayer(player, player.getPlayer().getDisplayName() + "" + ChatColor.YELLOW + " has finished!");
        }
    }
}