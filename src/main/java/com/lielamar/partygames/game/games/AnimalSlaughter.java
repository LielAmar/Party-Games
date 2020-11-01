package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.utils.Parameters;
import com.packetmanager.lielamar.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AnimalSlaughter extends Minigame implements Listener {

    private static int radius = 11;
    private static int maximum_amount_of_animals = 35;
    private static int minimum_amount_of_animals = 25;
    private static int negative_animals_percentage = 10;
    private static int negative_points_loss_percentage = 50;
    private static ItemStack item = SpigotUtils.getItem(Material.WOOD_SWORD, 1, ChatColor.YELLOW + "Slaughter Machine");
    private static Map<EntityType, Integer> animal_types = new HashMap<>();

    private Map<Entity, Integer> animals;

    public AnimalSlaughter(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.radius")) radius = config.getInt("parameters.radius");
        if(config.contains("parameters.maximum_amount_of_animals")) maximum_amount_of_animals = config.getInt("parameters.maximum_amount_of_animals");
        if(config.contains("parameters.minimum_amount_of_animals")) minimum_amount_of_animals = config.getInt("parameters.minimum_amount_of_animals");
        if(config.contains("parameters.negative_animals_percentage")) negative_animals_percentage = config.getInt("parameters.negative_animals_percentage");
        if(config.contains("parameters.negative_points_loss_percentage")) negative_points_loss_percentage = config.getInt("parameters.negative_points_loss_percentage");
        if(config.contains("parameters.item")) SpigotUtils.getItem(Material.valueOf(config.getString("parameters.item")), 1, ChatColor.YELLOW + "Slaughter Machine");
        if(config.contains("parameters.animals") && config.isConfigurationSection("parameters.animals")) {
            for(String s : config.getConfigurationSection("parameters.animals").getKeys(false))
                animal_types.put(EntityType.valueOf(s), config.getInt("parameters.animals." + s));
        } else {
            animal_types.put(EntityType.CHICKEN, 1);
            animal_types.put(EntityType.PIG, 3);
            animal_types.put(EntityType.COW, 5);
        }

        this.animals = new HashMap<>();

        try {
            super.validateVariables(
                    new IntValidation(radius, "[Animal Slaughter] Radius must be greater than 0", 1),
                    new IntValidation(maximum_amount_of_animals, "[Animal Slaughter] Max Amount of Animals must be greater than 0", 1),
                    new IntValidation(minimum_amount_of_animals, "[Animal Slaughter] Min Amount of Animals must be greater than 0", 1),
                    new IntValidation(negative_animals_percentage, "[Animal Slaughter] Negative Animals Percentage must be greater than/equals to 0 and less than/equals to 100", 0, 100),
                    new IntValidation(negative_points_loss_percentage, "[Animal Slaughter] Negative Point Loss Percentage must be greater than/equals to 0 and less than/equals to 100", 0, 100));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        this.spawnAnimals();

        for(CustomPlayer pl : super.getGame().getPlayers()) {
            if(pl == null) continue;
            pl.getPlayer().getInventory().addItem(item);
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(Entity ent : animals.keySet())
            ent.remove();
        animals = null;
    }


    /**
     * Spawns an X amount of animals whereas X = MAX_AMOUNT_OF_ANIMALS-CURRENT_AMOUNT_OF_ANIMALS+random
     *
     * Example (MAX_AMOUNT_OF_ANIMALS=50, CURRENT_AMOUNT_OF_ANIMALS=25, random=7)
     * X = 50-25+7 = 32 - The Function will spawn 32 animals
     */
    public void spawnAnimals() {
        int amount_of_animals = maximum_amount_of_animals - animals.size() + Main.rnd.nextInt(11);

        for(int i = 0; i < amount_of_animals; i++)
            this.spawnRandomAnimal();
    }

    /**
     * Spawns a random animal with a random amount of points
     */
    public void spawnRandomAnimal() {
        Entity ent = super.getMiddle().getWorld().spawnEntity(
                super.getMiddle().clone().add(Main.rnd.nextInt(radius*2+1)-radius,0, Main.rnd.nextInt(radius*2+1)-radius),
                (EntityType) animal_types.keySet().toArray()[Main.rnd.nextInt(animal_types.size())]);

        ent.setCustomNameVisible(true);
        if(ent instanceof Pig) ((Pig)ent).setSaddle(true);

        int percentage = new Random().nextInt(100)+1;
        if(percentage <= negative_animals_percentage) {
            ent.setCustomName(ChatColor.RED + "-" + negative_points_loss_percentage + "%");
            ((LivingEntity)ent).setHealth(1);
            this.animals.put(ent, 0);
        } else {
            int points = animal_types.get(ent.getType());

            ent.setCustomName(ChatColor.GREEN + "+" + points);
            ((LivingEntity)ent).setHealth(0.5 * points);
            this.animals.put(ent, points);
        }
    }

    /**
     * Handles the death of an entity by a player
     *
     * @param entity   Killed entity
     * @param player   Killer to whom we add point
     */
    public void handleAnimalKill(Entity entity, CustomPlayer player) {
        int score = animals.get(entity);
        animals.remove(entity);

        if(player == null) return;

        int tmpScore = player.getMinigameScore();
        if(score == 0) {
            tmpScore = tmpScore/(100/negative_points_loss_percentage);
            PacketManager.sendActionbar(player.getPlayer(), ChatColor.RED + "-" + negative_points_loss_percentage + "% score");
        } else {
            tmpScore = tmpScore + score;
            PacketManager.sendActionbar(player.getPlayer(), ChatColor.GREEN + "+" + score + " Score!");
        }
        player.setMinigameScore(tmpScore);
    }


    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof AnimalSlaughter)) return;

        e.setDroppedExp(0);
        e.getDrops().clear();

        if(!animal_types.containsKey(e.getEntity().getType())) return;
        if(!this.animals.containsKey(e.getEntity())) return;

        if(e.getEntity().getKiller() == null) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getEntity().getKiller());
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        CustomPlayer cp = super.getGame().getPlayers()[playerIndex];
        handleAnimalKill(e.getEntity(), cp);

        if(animals.size() < minimum_amount_of_animals)
            this.spawnAnimals();
    }

    @EventHandler
    public void onAnimalDamage(EntityDamageEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof AnimalSlaughter)) return;

        if(!animal_types.containsKey(e.getEntity().getType())) return;
        if(!this.animals.containsKey(e.getEntity())) return;

        e.setDamage(1);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onAnimalSpawn(EntitySpawnEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof AnimalSlaughter)) return;

        if(!animal_types.containsKey(e.getEntity().getType())) return;

        e.setCancelled(false);
    }

    @EventHandler
    public void onVehicle(VehicleEnterEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof AnimalSlaughter)) return;

        e.setCancelled(true);
    }
}