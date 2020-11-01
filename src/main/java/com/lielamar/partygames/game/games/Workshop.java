package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.TextUtils;
import com.lielamar.lielsutils.files.FileManager;
import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.models.entities.WorkshopKeeper;
import com.lielamar.partygames.models.games.Recipe;
import com.lielamar.partygames.models.games.WorkshopObject;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.*;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Workshop extends Minigame implements Listener {

    public static List<WorkshopKeeper> keepers = new ArrayList<>();

    private static String villager_prefix = ChatColor.GOLD + "Foreman Foreman: ";
    private static String villager_happy_message = ChatColor.YELLOW + "Perfect! That's just what I needed!";
    private static String villager_request_message = ChatColor.YELLOW + "Ok, so I need you to craft me a %s";
    private static int items_to_craft = 5;

    private Recipe[] recipes;
    private Map<CustomPlayer, WorkshopObject> assignedRecipeBoards;

    public Workshop(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.villager_prefix")) villager_prefix = config.getString("parameters.villager_prefix");
        if(config.contains("parameters.villager_happy_message")) villager_happy_message = config.getString("parameters.villager_happy_message");
        if(config.contains("parameters.villager_request_message")) villager_request_message = config.getString("parameters.villager_request_message");
        if(config.contains("parameters.items_to_craft")) items_to_craft = config.getInt("parameters.items_to_craft");

        this.generateRecipes();
    }

    @Override
    public void setupMinigame() {
        super.setupMinigame();
        FileManager.Config config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName());

        this.assignedRecipeBoards = new HashMap<>();

        int amount_of_canvases = config.getConfig().getConfigurationSection("workshops").getKeys(false).size();
        if(amount_of_canvases < super.getGame().getPlayers().length) {
            super.destroyMinigame();
            return;
        }

        WorkshopObject canvas;
        for(int i = 0; i < super.getGame().getPlayers().length; i++) {
            if(super.getGame().getPlayers()[i] == null) continue;

            List<Pair<Location, Location>> materialLocations = new ArrayList<>();
            for(int j = 0; j < config.getConfig().getConfigurationSection("workshops." + i + ".materials").getKeys(false).size(); j++)
                materialLocations.add(new Pair<>(
                        SpigotUtils.fetchLocation(config.getConfig(), "workshops." + i + ".materials." + j + ".start"),
                        SpigotUtils.fetchLocation(config.getConfig(), "workshops." + i + ".materials." + j + ".end")
                ));

            canvas = new WorkshopObject(
                    SpigotUtils.fetchLocation(config.getConfig(), "workshops." + i + ".recipeStart"),
                    SpigotUtils.fetchLocation(config.getConfig(), "workshops." + i + ".recipeEnd"),
                    SpigotUtils.fetchLocation(config.getConfig(), "workshops." + i + ".recipeProduct"),
                    SpigotUtils.fetchLocation(config.getConfig(), "workshops." + i + ".npc"),
                    materialLocations);

            this.assignedRecipeBoards.put(super.getGame().getPlayers()[i], canvas);
        }
    }

    @Override
    public void extraStartParameters() {
        for(int i = 0; i < super.getGame().getPlayers().length; i++) {
            if(super.getGame().getPlayers()[i] == null) continue;

            WorkshopObject workshop = assignedRecipeBoards.get(super.getGame().getPlayers()[i]);
            workshop.loadWorkshop(recipes[super.getGame().getPlayers()[i].getMinigameScore()]);

            WorkshopKeeper keeper = new WorkshopKeeper(workshop.getNPCLocation().getWorld());
            Workshop.keepers.add(keeper.spawnCustomEntity(workshop.getNPCLocation()));
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(WorkshopKeeper keeper : keepers) keeper.getBukkitEntity().remove();
        for(WorkshopObject workshop : assignedRecipeBoards.values()) workshop.destroy();
    }


    /**
     * Generates AMOUNT_OF_ITEMS_TO_CRAFT recipes
     */
    public void generateRecipes() {
        this.recipes = new Recipe[items_to_craft];
        int counter = 0;
        List<Recipe.RecipeType> rem = new ArrayList<>();

        for(int i = 0; i < this.recipes.length; i++) {
            Recipe.RecipeType recipe = Recipe.RecipeType.values()[Main.rnd.nextInt(Recipe.RecipeType.values().length)];
            while(rem.contains(recipe))
                recipe = Recipe.RecipeType.values()[Main.rnd.nextInt(Recipe.RecipeType.values().length)];

            recipes[counter] = new Recipe(recipe);
            counter++;
            rem.add(recipe);
//            boolean contains = true;
//            while(contains) {
//                recipe = Recipe.RecipeType.values()[Main.rnd.nextInt(Recipe.RecipeType.values().length)];
//                for(int j = 0; j < i; j++) {
//                    if(this.recipes[j].getRecipeType() == recipe) continue;
//                    contains = false;
//                }
//            }
//            this.recipes[i] = new Recipe(recipe);
        }
    }

    /**
     * Sends a message to the given player with the required material
     *
     * @param cp   {@link com.lielamar.partygames.models.CustomPlayer} to send message to
     */
    public void sendRecipeMessage(CustomPlayer cp) {
        if(cp == null) return;

        if(cp.getMinigameScore() > 0)
            cp.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', villager_prefix) + ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', villager_happy_message));

        cp.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', villager_prefix) + ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&',
                String.format(villager_request_message, TextUtils.enumToString(recipes[cp.getMinigameScore()].getRecipeType().getProduct().name()))));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        if(e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;
        if(super.getGame().getPlayers()[playerIndex].getMinigameScore() >= recipes.length) return;

        if(e.getClickedBlock().getType() != Material.FURNACE && e.getClickedBlock().getType() != Material.WORKBENCH)
            e.setCancelled(true);

        if(e.getClickedBlock().getType() == Material.BURNING_FURNACE) {
            e.setCancelled(false);

            org.bukkit.material.Furnace data = new org.bukkit.material.Furnace(Material.BURNING_FURNACE);
            data.setFacingDirection(e.getClickedBlock().getFace(e.getClickedBlock()));

            e.getClickedBlock().setType(Material.FURNACE);
            ((Furnace)e.getClickedBlock()).setData(data);
        }

        if(!assignedRecipeBoards.containsKey(super.getGame().getPlayers()[playerIndex])) return;
        if(!assignedRecipeBoards.get(super.getGame().getPlayers()[playerIndex]).containsMaterial(e.getClickedBlock())) return;

        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.DIG_STONE, 1F, 1F);
        e.getPlayer().getInventory().addItem(WorkshopObject.getMatchingMaterial(e.getClickedBlock().getType()));
        e.getClickedBlock().setType(Material.AIR);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractWithVillager(PlayerInteractAtEntityEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        e.setCancelled(true);

        if(e.getRightClicked() == null) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(e.getRightClicked() instanceof ItemFrame) {
            e.setCancelled(true);
            return;
        }

        if(!(e.getRightClicked() instanceof Villager)) return;

        CustomPlayer cp = super.getGame().getPlayers()[playerIndex];
        if(cp.getMinigameScore() == recipes.length) return;

        Material product = recipes[cp.getMinigameScore()].getRecipeType().getProduct();
        if(!e.getPlayer().getInventory().contains(product)
                && e.getPlayer().getInventory().getHelmet().getType() != product
                && e.getPlayer().getInventory().getChestplate().getType() != product
                && e.getPlayer().getInventory().getLeggings().getType() != product
                && e.getPlayer().getInventory().getBoots().getType() != product) return;

        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setHelmet(null);
        e.getPlayer().getInventory().setChestplate(null);
        e.getPlayer().getInventory().setLeggings(null);
        e.getPlayer().getInventory().setBoots(null);
        cp.addMinigameScore(1);

        if(cp.getMinigameScore() == recipes.length) {
            super.finishPlayer(super.getGame().getPlayers()[playerIndex], e.getPlayer().getDisplayName() + "" + ChatColor.YELLOW + " has finished the game!");
//            super.initialSpectator(super.getGame().getPlayers()[playerIndex], false, false, 0);
        } else {
            assignedRecipeBoards.get(cp).loadWorkshop(recipes[cp.getMinigameScore()]);
            sendRecipeMessage(cp);
        }
    }

    @EventHandler
    public void onFurnaceOpen(InventoryOpenEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        if(e.getInventory().getType() == InventoryType.MERCHANT) {
            e.setCancelled(true);
            return;
        }

        if(e.getInventory().getType() != InventoryType.FURNACE) return;

        e.getInventory().setItem(1, new ItemStack(Material.COAL, 64));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        e.setCancelled(false);

        if(e.getClickedInventory() == null || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        if(e.getClickedInventory().getType() == InventoryType.FURNACE)
            e.setCancelled(e.getCurrentItem().getType() == Material.COAL);
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        Furnace furnace = (Furnace) e.getBlock().getState();
        furnace.setCookTime((short) 200);
        furnace.setBurnTime((short)20);
        furnace.update();
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        Furnace furnace = (Furnace) e.getBlock().getState();
        furnace.setCookTime((short) 200);
        furnace.setBurnTime((short)20);
        furnace.update();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemFrameDamage(EntityDamageByEntityEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        if(e.getEntity() instanceof ItemFrame)
            e.setCancelled(true);
    }

    @EventHandler
    public void onItemFrameBreak(HangingBreakByEntityEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        if(e.getEntity() instanceof ItemFrame)
            e.setCancelled(true);
    }

    @EventHandler
    public void onItemFrameInteract(PlayerInteractEntityEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Workshop)) return;

        if(e.getRightClicked() instanceof ItemFrame)
            e.setCancelled(true);
    }
}