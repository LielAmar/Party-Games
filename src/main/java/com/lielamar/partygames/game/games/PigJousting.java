package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.entities.custom.ControllablePig;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PigJousting extends Minigame implements Listener {

    private static boolean constant_movement = false;
    private static ItemStack helmet = new ItemStack(Material.IRON_HELMET);
    private static ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
    private static ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
    private static ItemStack boots = new ItemStack(Material.IRON_BOOTS);
    private static ItemStack sword = new ItemStack(Material.IRON_SWORD);

    public static Map<UUID, ControllablePig> pigs = new HashMap<>();

    public PigJousting(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + getMinigameName()).getConfig();

        if(config.contains("parameters.constant_movement")) constant_movement = config.getBoolean("parameters.constant_movement");
        if(config.contains("parameters.helmet")) helmet = new ItemStack(Material.valueOf(config.getString("parameters.helmet")));
        if(config.contains("parameters.chestplate")) chestplate = new ItemStack(Material.valueOf(config.getString("parameters.chestplate")));
        if(config.contains("parameters.leggings")) leggings = new ItemStack(Material.valueOf(config.getString("parameters.leggings")));
        if(config.contains("parameters.boots")) boots = new ItemStack(Material.valueOf(config.getString("parameters.boots")));
        if(config.contains("parameters.sword")) sword = new ItemStack(Material.valueOf(config.getString("parameters.sword")));

        this.setupPigs();
    }

    @Override
    public void extraStartParameters() {
        this.startAdditionalTimer();

        Location start, end;
        for(int i = 0; i < super.getLocations().length; i++) {
            start = SpigotUtils.fetchLocation(super.getGame().getMain().getFileManager()
                    .getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig(), "fences." + i + ".start");
            end = SpigotUtils.fetchLocation(super.getGame().getMain().getFileManager()
                    .getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig(), "fences." + i + ".end");

            for(int x = 0; Math.abs(x) <= Math.abs(start.getX()-end.getX()); x+= (start.getX() > end.getX()) ? -1 : 1) {
                for(int y = 0; Math.abs(y) <= Math.abs(start.getY()-end.getY()); y+= (start.getY() > end.getY()) ? -1 : 1) {
                    for(int z = 0; Math.abs(z) <= Math.abs(start.getZ()-end.getZ()); z+= (start.getZ() > end.getZ()) ? -1 : 1)
                        start.clone().add(x, y, z).getBlock().setType(Material.AIR);
                }
            }
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            super.getGame().getMain().getPacketReader().eject(cp.getPlayer());
        }

        for(ControllablePig pig : pigs.values())
            pig.destroyCustomEntity(pig);
        pigs = new HashMap<>();
    }


    /**
     * An additional timer running alongside the main minigame timer
     * Starts damaging alive players when there are 30 seconds remaining
     */
    public void startAdditionalTimer() {
        new BukkitRunnable() {
            int i = getGameTime();

            @Override
            public void run() {
                if(i == 0 || getGameState() != GameState.IN_GAME) {
                    this.cancel();
                    return;
                }

                if(i <= 30 && i%2 == 0) {
                    for(CustomPlayer cp : getGame().getPlayers()) {
                        if(cp == null) continue;
                        if(!cp.isSpectator())
                            cp.getPlayer().damage(1);
                    }
                }

                i--;
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, 20L);
    }

    /**
     * Sets up all pigs & players
     */
    public void setupPigs() {
        new BukkitRunnable() {

            @Override
            public void run() {
                ControllablePig pig;

                for(CustomPlayer cp : getGame().getPlayers()) {
                    if(cp == null) continue;

                    getGame().getMain().getPacketReader().inject(cp.getPlayer());

                    pig = new ControllablePig(getGame().getMain(), cp.getPlayer().getWorld());
                    pig.spawnCustomEntity(pig, cp.getPlayer().getLocation());
                    if(cp.getPlayer().isSneaking())
                        cp.getPlayer().setSneaking(false);

                    pig.getBukkitEntity().setPassenger(cp.getPlayer());
                    pig.getControllableEntityHandler().setCanMove(true);
                    pig.getControllableEntityHandler().setConstantMovement(constant_movement);

                    pigs.put(cp.getPlayer().getUniqueId(), pig);

                    cp.getPlayer().getInventory().setHelmet(helmet);
                    cp.getPlayer().getInventory().setChestplate(chestplate);
                    cp.getPlayer().getInventory().setLeggings(leggings);
                    cp.getPlayer().getInventory().setBoots(boots);
                    cp.getPlayer().getInventory().addItem(sword);
                    cp.getPlayer().updateInventory();
                }
            }
        }.runTaskLater(super.getGame().getMain(), 3L);
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof PigJousting)) return;

        e.setCancelled(true);

        if(super.getGameState() != GameState.IN_GAME) return;

        if(!(e.getEntity() instanceof Player)) return;
        if(!(e.getDamager() instanceof Player)) return;

        int playerIndex = super.getGame().getPlayerIndex(((Player)e.getDamager()));
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        playerIndex = super.getGame().getPlayerIndex(((Player)e.getEntity()));
        if(playerIndex == -1) return;
        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        e.setCancelled(false);

        Player p = (Player)e.getEntity();

        if(p.getHealth()-e.getDamage() <= 0) {
            e.setCancelled(true);
            super.initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
            super.getGame().infoPlayers(ChatColor.RED + p.getName() + ChatColor.YELLOW + " has been killed by " + ChatColor.GREEN + e.getDamager().getName());
            p.setHealth(20);

            super.getGame().getMain().getPacketReader().eject(p);
            if(pigs.containsKey(p.getUniqueId())) {
                pigs.get(p.getUniqueId()).getBukkitEntity().setPassenger(null);
                pigs.get(p.getUniqueId()).getWorld().removeEntity(pigs.get(p.getUniqueId()));
                pigs.remove(p.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof PigJousting)) return;

        if(!(e.getEntity() instanceof Player)) return;

        e.setCancelled(true);
    }
}