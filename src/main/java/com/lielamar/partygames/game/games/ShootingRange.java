package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.entities.custom.ShootingRangeSkeleton;
import com.lielamar.partygames.modules.entities.custom.ShootingRangeZombie;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShootingRange extends Minigame implements Listener {

    /**
     * Developer Notes:
     * If some mobs does not move if they are far from the players, you need to change your spigot configuration.
     * Reference: https://www.spigotmc.org/threads/guide-server-optimization%E2%9A%A1.283181/
     * Section: Spigot.yml (entity-activation-range)
     */

    private static int amount_of_runners = 10;
    private static int normal_zombie_percentage = 40;
    private static int normal_skeleton_percentage = 32;
    private static int golden_zombie_percentage = 16;
    private static int tnt_zombie_percentage = 12;

    private List<Pair<Location, Location>> mobSpawnLocations;
    private List<UUID> explosiveArrow;
    private List<Entity> mobs;

    public ShootingRange(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.amount_of_runners")) amount_of_runners = config.getInt("parameters.amount_of_runners");
        if(config.contains("parameters.normal_zombie_percentage")) normal_zombie_percentage = config.getInt("parameters.normal_zombie_percentage");
        if(config.contains("parameters.normal_skeleton_percentage")) normal_skeleton_percentage = config.getInt("parameters.normal_skeleton_percentage");
        if(config.contains("parameters.golden_zombie_percentage")) golden_zombie_percentage = config.getInt("parameters.golden_zombie_percentage");
        if(config.contains("parameters.tnt_zombie_percentage")) tnt_zombie_percentage = config.getInt("parameters.tnt_zombie_percentage");

        this.explosiveArrow = new ArrayList<>();
        this.mobs = new ArrayList<>();

        this.setupMobSpawnLocations(config);

        try {
            super.validateVariables(
                    new IntValidation(amount_of_runners, "[Shooting Range] Amount of Runners must be greater than 0", 1),
                    new IntValidation(normal_zombie_percentage, "[Shooting Range] Normal Zombie Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100),
                    new IntValidation(normal_skeleton_percentage, "[Shooting Range] Normal Skeleton Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100),
                    new IntValidation(golden_zombie_percentage, "[Shooting Range] Golden Zombie Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100),
                    new IntValidation(tnt_zombie_percentage, "[Shooting Range] TNT Zombie Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        ItemStack bow = SpigotUtils.getItem(Material.BOW, 1, null, null, Enchantment.ARROW_INFINITE);
        ItemStack arrow = new ItemStack(Material.ARROW);

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            cp.getPlayer().getInventory().addItem(bow);
            cp.getPlayer().getInventory().addItem(arrow);
        }

        spawnMobs();
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(Entity ent : mobs)
            ent.remove();
        mobs.clear();
    }


    /**
     * Sets up all spawn locations for the mobs
     *
     * @param config   Config to load data from
     */
    public void setupMobSpawnLocations(YamlConfiguration config) {
        this.mobSpawnLocations = new ArrayList<>();

        for(int i = 0; i < config.getConfigurationSection("mobSpawnLocations").getKeys(false).size(); i++)
            this.mobSpawnLocations.add(new Pair<>(SpigotUtils.fetchLocation(config, "mobSpawnLocations." + i + ".start"),
                    SpigotUtils.fetchLocation(config, "mobSpawnLocations." + i + ".end")));
    }

    /**
     * Spawns X amount of mobs (depending on current amount)
     */
    public void spawnMobs() {
        new BukkitRunnable() {
            int randomValue = Main.rnd.nextInt(5)-2;

            @Override
            public void run() {
                if(getGameState() != GameState.IN_GAME && getGameState() != GameState.COUNTING_DOWN)
                    this.cancel();

                if(mobs.size() < amount_of_runners + randomValue) mobs.add(spawnMob());
                else this.cancel();
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 5L);
    }

    /**
     * Spawns a random mob
     *
     * @return   Spawned mob
     */
    public Entity spawnMob() {
        int percentage = Main.rnd.nextInt(normal_zombie_percentage + normal_skeleton_percentage + golden_zombie_percentage + tnt_zombie_percentage);

        Pair<Location,Location> randomPair = mobSpawnLocations.get(Main.rnd.nextInt(mobSpawnLocations.size()));
        if(System.currentTimeMillis()%2 == 0) { // Swapping locations
            Location tmp = randomPair.getKey();
            randomPair.setKey(randomPair.getValue());
            randomPair.setValue(tmp);
        }

        if(percentage < normal_zombie_percentage) {
            ShootingRangeZombie zombie = new ShootingRangeZombie(middle.getWorld(), randomPair.getKey(), randomPair.getValue(), 1.5);
            return zombie.spawnCustomEntity(zombie, randomPair.getKey()).getBukkitEntity();
        } else if(percentage < normal_zombie_percentage + normal_skeleton_percentage) {
            ShootingRangeSkeleton skeleton = new ShootingRangeSkeleton(middle.getWorld(), randomPair.getKey(), randomPair.getValue(), 1.6);
            return skeleton.spawnCustomEntity(skeleton, randomPair.getKey()).getBukkitEntity();
        } else if(percentage < normal_zombie_percentage + normal_skeleton_percentage + golden_zombie_percentage) {
            ShootingRangeZombie zombie = new ShootingRangeZombie(middle.getWorld(), randomPair.getKey(), randomPair.getValue(), 2);
            ((LivingEntity)zombie.getBukkitEntity()).getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET));
            ((LivingEntity)zombie.getBukkitEntity()).getEquipment().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
            ((LivingEntity)zombie.getBukkitEntity()).getEquipment().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
            ((LivingEntity)zombie.getBukkitEntity()).getEquipment().setBoots(new ItemStack(Material.GOLD_BOOTS));
            zombie.setCustomName(ChatColor.GOLD + "SUPER");
            zombie.setCustomNameVisible(false);
            return zombie.spawnCustomEntity(zombie, randomPair.getKey()).getBukkitEntity();
        } else {
            ShootingRangeZombie zombie = new ShootingRangeZombie(middle.getWorld(), randomPair.getKey(), randomPair.getValue(), 2.1);
            ((LivingEntity)zombie.getBukkitEntity()).getEquipment().setHelmet(new ItemStack(Material.TNT));
            zombie.setCustomName(ChatColor.RED + "TNT");
            zombie.setCustomNameVisible(false);
            return zombie.spawnCustomEntity(zombie, randomPair.getKey()).getBukkitEntity();
        }
    }

    /**
     * Checks if an entity is valid and if so, it kills it, adds points to the killer and some more cool stuff.
     *
     * @param entity   Entity to check
     * @param cp       Attached Player (Killer)
     */
    public void checkEntity(Entity entity, CustomPlayer cp) {
        if(!(entity instanceof LivingEntity)) return;

        if(cp != null) {
            if(entity.getType() == EntityType.SKELETON)
                cp.addMinigameScore(1);
            if(entity.getType() == EntityType.ZOMBIE && entity.getCustomName() != null && entity.getCustomName().equalsIgnoreCase(ChatColor.RED + "TNT")) {
                cp.addMinigameScore(1);
                this.explosiveArrow.add(cp.getPlayer().getUniqueId());
            } else if(entity.getType() == EntityType.ZOMBIE && entity.getCustomName() != null && entity.getCustomName().equalsIgnoreCase(ChatColor.GOLD + "SUPER"))
                cp.addMinigameScore(10);
            else if(entity.getType() == EntityType.ZOMBIE)
                cp.addMinigameScore(1);

            cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.SUCCESSFUL_HIT, 1F, 1F);
        }

        ((LivingEntity)entity).setHealth(0);
    }


    @EventHandler
    public void onMobShoot(EntityDamageByEntityEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof ShootingRange)) return;

        if(e.getEntity() instanceof Player || e.getDamager() instanceof Player) return;
        if(!(e.getDamager() instanceof Arrow)) return;
        if(!(((Arrow)e.getDamager()).getShooter() instanceof Player)) return;

        Player p = (Player) ((Arrow)e.getDamager()).getShooter();

        int playerIndex = super.getGame().getPlayerIndex(p);
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        checkEntity(e.getEntity(), super.getGame().getPlayers()[playerIndex]);
    }

    @EventHandler
    public void onArrowLand(ProjectileHitEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof ShootingRange)) return;

        if(!(e.getEntity() instanceof Arrow)) return;
        if(!(e.getEntity().getShooter() instanceof Player)) return;

        Player shooter = (Player)e.getEntity().getShooter();
        int playerIndex = super.getGame().getPlayerIndex(shooter);
        if(playerIndex == -1) return;

        if(this.explosiveArrow.contains(shooter.getUniqueId())) {
            this.explosiveArrow.remove(shooter.getUniqueId());
            e.getEntity().getLocation().getWorld().playEffect(e.getEntity().getLocation(), Effect.EXPLOSION_HUGE, 5);
            super.getGame().playSound(Sound.EXPLODE, 0.7F, 1F);
            for(Entity ent : e.getEntity().getLocation().getWorld().getNearbyEntities(e.getEntity().getLocation(), 1.5, 1.5, 1.5))
                checkEntity(ent, super.getGame().getPlayers()[playerIndex]);
        }
        e.getEntity().remove();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof ShootingRange)) return;

        if(e.getEntity().getType() == EntityType.SKELETON || e.getEntity().getType() == EntityType.ZOMBIE) {
            e.setDroppedExp(0);
            if(mobs.contains(e.getEntity())) {
                spawnMobs();
                mobs.remove(e.getEntity());
            }
        }
    }

    @EventHandler
    public void onDropSpawn(EntitySpawnEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof ShootingRange)) return;

        if(e.getEntity().getType() == EntityType.DROPPED_ITEM)
            e.setCancelled(true);
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof ShootingRange)) return;

        e.setCancelled(true);
    }
}