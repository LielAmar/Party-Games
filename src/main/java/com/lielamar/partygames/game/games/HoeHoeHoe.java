package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.utils.GameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoeHoeHoe extends Minigame implements Listener {

    private static Integer[] wool_colors = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 14 };
    private static ItemStack item = SpigotUtils.getItem(Material.WOOD_HOE, 1, ChatColor.YELLOW + "Hoe Hoe Hoe");
    private static List<Material> block_types = new ArrayList<>();

    private Map<Integer, CustomPlayer> assignedColors;
    private List<Player> didntStartGame;

    public HoeHoeHoe(Game game, GameType gameType) {
        super(game, gameType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        if(config.contains("parameters.item")) item = SpigotUtils.getItem(Material.valueOf(config.getString("parameters.item")), 1, ChatColor.YELLOW + "Hoe Hoe Hoe");
        if(config.contains("parameters.block_types") && config.isList("parameters.block_types")) {
            for(String s : config.getStringList("parameters.block_types"))
                block_types.add(Material.valueOf(s));
        } else {
            block_types.add(Material.GRASS);
            block_types.add(Material.MYCEL);
        }
        if(config.contains("parameters.wool_colors") && config.isList("parameters.wool_colors"))
            wool_colors = config.getIntegerList("parameters.wool_colors").toArray(new Integer[0]);

        this.assignedColors = new HashMap<>();
        this.didntStartGame = new ArrayList<>();

        GameUtils.assignColorsToPlayers(super.getGame().getPlayers(), wool_colors, this.assignedColors);

        try {
            super.validateVariables(
                            new IntValidation(wool_colors.length, "[Hoe Hoe Hoe] Amount of Colors must be greater than/equals to Amount Of Players", super.getGame().getPlayers().length));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        super.teleportPlayers();

        for(int i = 0; i < super.getGame().getPlayers().length; i++) {
            if(super.getGame().getPlayers()[i] == null) continue;

            super.getGame().getPlayers()[i].getPlayer().getInventory().addItem(item);
            this.didntStartGame.add(super.getGame().getPlayers()[i].getPlayer());
        }
    }


    /**
     * Checks whether or not a block in a location is a carpet with the CustomPlayer color
     *
     * @param location   Location of the block
     * @param cp         CustomPlayer to check color of
     */
    @SuppressWarnings("deprecation")
    public boolean isCarpet(Location location, CustomPlayer cp) {
        return (location.getBlock().getType() == Material.CARPET && location.getBlock().getData() == GameUtils.getPlayerAssignedColor(cp, this.assignedColors));
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    @SuppressWarnings("deprecation")
    public void onInteract(PlayerInteractEvent e) {
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof HoeHoeHoe)) return;

        e.setCancelled(true);

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(!e.getPlayer().getItemInHand().equals(item)) return;
        if(e.getClickedBlock() == null || !block_types.contains(e.getClickedBlock().getType())) return;

        CustomPlayer cp = super.getGame().getPlayers()[playerIndex];

        if(this.didntStartGame.contains(e.getPlayer())) {
            this.didntStartGame.remove(e.getPlayer());
            e.getClickedBlock().getLocation().clone().add(0, 1, 0).getBlock().setType(Material.CARPET);
            e.getClickedBlock().getLocation().clone().add(0, 1, 0).getBlock().setData((byte) GameUtils.getPlayerAssignedColor(cp, this.assignedColors));
            cp.addMinigameScore(1);
            return;
        }


        Location loc1 = e.getClickedBlock().getLocation().clone().add(1,1,0);
        Location loc2 = e.getClickedBlock().getLocation().clone().add(-1,1,0);
        Location loc3 = e.getClickedBlock().getLocation().clone().add(0,1,1);
        Location loc4 = e.getClickedBlock().getLocation().clone().add(0,1,-1);

        if(isCarpet(loc1, cp) || isCarpet(loc2, cp) || isCarpet(loc3, cp) || isCarpet(loc4, cp)) {
            e.getClickedBlock().getLocation().clone().add(0, 1, 0).getBlock().setType(Material.CARPET);
            e.getClickedBlock().getLocation().clone().add(0, 1, 0).getBlock().setData((byte)GameUtils.getPlayerAssignedColor(cp, this.assignedColors));
            cp.addMinigameScore(1);
        }
    }
}