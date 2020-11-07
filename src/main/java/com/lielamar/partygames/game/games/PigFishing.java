package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.DoubleValidation;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.*;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.utils.GameUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PigFishing extends Minigame implements Listener {

    private static double max_distance_from_location =  5.0;
    private static double pig_score_y = 95.0;
    private static int amount_of_pigs = 15;
    private static int normal_pig_percentage = 70;
    private static int baby_percentage = 25;
    private static int special_percentage = 5;
    private static Integer[] wool_colors = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 14 };

    private Map<Integer, CustomPlayer> assignedColors;
    private Location[] redstoneLocations;
    private Location pigtile;
    private List<Pig> pigs;

    public PigFishing(Game game, GameType gameType) {
        super(game, gameType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        if(config.contains("parameters.max_distance_from_location")) max_distance_from_location = config.getDouble("parameters.max_distance_from_location");
        if(config.contains("parameters.pig_score_y")) pig_score_y = config.getDouble("parameters.pig_score_y");
        if(config.contains("parameters.amount_of_pigs")) amount_of_pigs = config.getInt("parameters.amount_of_pigs");
        if(config.contains("parameters.normal_pig_percentage")) normal_pig_percentage = config.getInt("parameters.normal_pig_percentage");
        if(config.contains("parameters.baby_percentage")) baby_percentage = config.getInt("parameters.baby_percentage");
        if(config.contains("parameters.special_percentage")) special_percentage = config.getInt("parameters.special_percentage");
        if(config.contains("parameters.wool_colors") && config.isList("parameters.wool_colors"))
            wool_colors = config.getIntegerList("parameters.wool_colors").toArray(new Integer[0]);

        this.assignedColors = new HashMap<>();
        this.redstoneLocations = SpigotUtils.fetchLocations(config, "redstoneblocks");
        this.pigtile = SpigotUtils.fetchLocation(config, "pigtile");
        this.pigs = new ArrayList<>();

        GameUtils.assignColorsToPlayers(super.getGame().getPlayers(), wool_colors, this.assignedColors);

        try {
            super.validateVariables(
                    new DoubleValidation(max_distance_from_location, "[Pig Fishing] Max Distance From Location must be greater than 0", 1),
                    new IntValidation(amount_of_pigs, "[Pig Fishing] Amount of Pigs must be greater than 0", 1),
                    new IntValidation(normal_pig_percentage, "[Pig Fishing] Normal Pig Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100),
                    new IntValidation(baby_percentage, "[Pig Fishing] Baby Pig Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100),
                    new IntValidation(special_percentage, "[Pig Fishing] Special Pig Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100),
                    new IntValidation(wool_colors.length, "[Pig Fishing] Amount of Colors must be greater than/equals to Amount Of Players", super.getGame().getPlayers().length));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        this.startAdditionalTimer();

        ItemStack rod = SpigotUtils.getItem(Material.FISHING_ROD);

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;

            cp.getPlayer().getInventory().addItem(rod);
            cp.getPlayer().updateInventory();
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(Pig pig : this.pigs)
            pig.remove();
        this.pigs = null;
    }


    /**
     * An additional timer running alongside the main minigame timer
     * Spawns 2/0 pigs (depending on current amount of pigs) and checking all existing pigs
     */
    public void startAdditionalTimer() {
        new BukkitRunnable() {
            int i = getGameType().getGameDuration();
            int randomValue = Main.rnd.nextInt(5)-2;

            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    return;
                }

                if(getGameState() == GameState.IN_GAME && i%2 == 0 && pigs.size() <= amount_of_pigs + randomValue)
                    spawnPigs(2);

                checkPigs();

                i--;
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 20L);
    }

    /**
     * Spawns X amount of pigs
     *
     * @param amount   Amount of pigs to spawn
     */
    public void spawnPigs(int amount) {
        int percentage;
        Pig pig;

        for(int i = 0; i < amount; i++) {
            percentage = Main.rnd.nextInt(normal_pig_percentage + baby_percentage + special_percentage);

            pig = (Pig) pigtile.getWorld().spawnEntity(pigtile, EntityType.PIG);
            pig.setSaddle(true);

            if(percentage > normal_pig_percentage && percentage < normal_pig_percentage + baby_percentage) {
                pig.setBaby();
            } else if(percentage > normal_pig_percentage + baby_percentage) {
                pig.setBaby();
                pig.setCustomName(ChatColor.GOLD + "Super Pig");
                pig.setCustomNameVisible(true);
            }

            this.pigs.add(pig);
        }
    }

    /**
     * Checks all spawned pigs to see if we need to update some player's points
     */
    public void checkPigs() {
        List<Pig> rem = new ArrayList<>();

        for(Pig pig : this.pigs) {
            if(pig.getLocation().getY() < pig_score_y) {
                if(pig.getLocation() == null || pig.getLocation().getBlock() == null
                        || pig.getLocation().getBlock().getRelative(BlockFace.DOWN) == null) continue;

                if(pig.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.WOOL) {
                    int color = SpigotUtils.translateColorToInt(((Wool)pig.getLocation().getBlock().getRelative(BlockFace.DOWN).getState().getData()).getColor());
                    if(!assignedColors.containsKey(color)) continue;

                    CustomPlayer cp = assignedColors.get(color);
                    if(cp == null) continue;
                    cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);

                    if(pig.getCustomName() != null) {
                        if (pig.getCustomName().equalsIgnoreCase(ChatColor.GOLD + "Super Pig"))
                            activateRedstoneBlocks(cp);
                    }
                    cp.addMinigameScore(1);
                    pig.remove();
                    rem.add(pig);
                }
            }
        }

        for(Pig pig : rem) {
            this.pigs.remove(pig);
        }
    }

    /**
     * Activates all redstone blocks except for the given player
     *
     * @param cp   {@link CustomPlayer} to avoid activating redstone blocks of
     */
    public void activateRedstoneBlocks(CustomPlayer cp) {
        Location cpLocation = null;
        if(cp != null) {
            int playerIndex = super.getGame().getPlayerIndex(cp.getPlayer());
            if(playerIndex != -1)
                cpLocation = super.getLocations()[playerIndex];
        }

        List<Block> changedBlocks = new ArrayList<>();
        for(Location redstone : this.redstoneLocations) {
            if(cpLocation != null) {
                if(Math.sqrt(Math.pow(cpLocation.getX()-redstone.getX(), 2) + Math.pow(cpLocation.getZ()-redstone.getZ(), 2)) > 7) {
                    redstone.getBlock().setType(Material.REDSTONE_BLOCK);
                    changedBlocks.add(redstone.getBlock());
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for(Block b : changedBlocks)
                    b.setType(Material.WOOD);
            }
        }.runTaskLater(super.getGame().getMain(), 100L);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof PigFishing)) return;

        if(super.getGameState() == GameState.GAME_END) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        boolean preGame = super.getGameState() != GameState.IN_GAME;
        boolean blockChanged = (int)e.getTo().getX() != (int)e.getFrom().getX()
                || (int)e.getTo().getY() != (int)e.getFrom().getY()
                || (int)e.getTo().getZ() != (int)e.getFrom().getZ();
        if(preGame && blockChanged) {
            e.setTo(e.getFrom());
            return;
        }

        if(e.getTo().distance(super.getLocations()[playerIndex]) > max_distance_from_location)
            e.setTo(super.getLocations()[playerIndex]);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof PigFishing)) return;

        if(e.getEntity() instanceof Pig) {
            if(e.getDamage() > 0)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleEnter(PlayerInteractAtEntityEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof PigFishing)) return;

        if(e.getRightClicked() instanceof Pig)
            e.setCancelled(true);
    }
}