package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.CharValidation;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.utils.GameUtils;
import com.lielamar.partygames.utils.Parameters;
import com.packetmanager.lielamar.PacketManager;
import net.minecraft.server.v1_8_R3.EntityArrow;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MinecartRacing extends Minigame implements Listener {

    private static char axis = 'x';
    private static Integer[] wool_colors = new Integer[]{0, 1, 2, 3, 4, 5, 6, 14};

    private Map<Integer, CustomPlayer> assignedColors;
    private Map<UUID, Minecart> carts;
    private Location finishLine;
    private Location[] rails;

    public MinecartRacing(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if (config.contains("parameters.axis")) axis = config.getString("parameters.axis").toLowerCase().charAt(0);
        if (config.contains("parameters.wool_colors") && config.isList("parameters.wool_colors"))
            wool_colors = config.getIntegerList("parameters.wool_colors").toArray(new Integer[0]);

        this.assignedColors = new HashMap<>();
        this.carts = new HashMap<>();
        this.finishLine = SpigotUtils.fetchLocation(config, "finish");

        GameUtils.assignColorsToPlayers(super.getGame().getPlayers(), wool_colors, this.assignedColors);
        this.setupCarts(config);
        this.setupBoards(config);

        try {
            super.validateVariables(
                    new CharValidation(axis, "[Minecart Racing] Axis must be from the allowed Axes list: x/z", new Character[]{'x', 'z'}),
                    new IntValidation(wool_colors.length, "[Minecart Racing] Amount of Colors must be greater than/equals to Amount Of Players", super.getGame().getPlayers().length));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        ItemStack bow = SpigotUtils.getItem(Material.BOW, 1, null, null, Enchantment.ARROW_INFINITE);
        ItemStack arrow = new ItemStack(Material.ARROW);

        for (CustomPlayer cp : super.getGame().getPlayers()) {
            if (cp == null) continue;

            cp.getPlayer().getInventory().addItem(bow);
            cp.getPlayer().getInventory().addItem(arrow);

            if (cp.getPlayer().isSneaking())
                cp.getPlayer().setSneaking(false);
            this.carts.get(cp.getPlayer().getUniqueId()).setPassenger(cp.getPlayer());

            ItemStack color = new ItemStack(Material.WOOL, 1, (byte) super.getGame().getPlayerIndex(cp.getPlayer()));

            for (int i = 0; i < 7; i++)
                cp.getPlayer().getInventory().setItem(i + 2, color);
            cp.getPlayer().updateInventory();
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for (Minecart minecart : carts.values())
            minecart.remove();
    }


    /**
     * Sets up all carts
     */
    public void setupCarts(YamlConfiguration config) {
        this.rails = SpigotUtils.fetchLocations(config, "rails");

        if (this.rails.length < super.getGame().getPlayers().length) {
            super.destroyMinigame();
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < rails.length; i++) {
                    if (getGame().getPlayers()[i] == null) continue;

                    Minecart cart = (Minecart) rails[i].getWorld().spawnEntity(rails[i], EntityType.MINECART);
                    cart.setMaxSpeed(0.4D * 2);
                    carts.put(getGame().getPlayers()[i].getPlayer().getUniqueId(), cart);
                }
            }
        }.runTaskLater(super.getGame().getMain(), 2L);
    }

    /**
     * Sets up all boards
     */
    @SuppressWarnings("deprecation")
    public void setupBoards(YamlConfiguration config) {
        int distance = config.getInt("boards.distance");
        int endpoint = config.getInt("boards.endpoint");

        Location start, end;
        Block b;
        for (int i = 0; i < 4; i++) {
            start = SpigotUtils.fetchLocation(config, "boards." + i + ".start");
            end = SpigotUtils.fetchLocation(config, "boards." + i + ".end");

            if (axis == 'x') {
                int amount_of_blocks = (int) Math.abs(Math.abs(start.getX()) - Math.abs(endpoint)) / distance;
                for (int j = 0; j <= amount_of_blocks; j++) {
                    for (int z = (int) start.getZ(); (start.getZ() > end.getZ()) ? z >= end.getZ() : z <= end.getZ(); z += (start.getZ() > end.getZ()) ? -1 : 1) {
                        for (int y = (int) start.getY(); (start.getY() > end.getY()) ? y >= end.getY() : y <= end.getY(); y += (start.getY() > end.getY()) ? -1 : 1) {
                            b = start.getWorld().getBlockAt((int) start.getX() + j * distance, y, z);
                            if (b.getType() != Material.WOOL) continue;
                            b.setData(wool_colors[Main.rnd.nextInt(wool_colors.length)].byteValue(), true);
                        }
                    }
                }
            } else {
                int amount_of_blocks = (int) Math.abs(Math.abs(start.getZ()) - Math.abs(endpoint)) / distance;
                for (int j = 0; j <= amount_of_blocks; j++) {
                    for (int x = (int) start.getX(); (start.getX() > end.getX()) ? x >= end.getX() : x <= end.getX(); x += (start.getX() > end.getX()) ? -1 : 1) {
                        for (int y = (int) start.getY(); (start.getY() > end.getY()) ? y >= end.getY() : y <= end.getY(); y += (start.getY() > end.getY()) ? -1 : 1) {
                            b = start.getWorld().getBlockAt(x, y, (int) start.getZ() + j * distance);
                            if (b.getType() != Material.WOOL) continue;
                            b.setData(wool_colors[Main.rnd.nextInt(wool_colors.length)].byteValue(), true);
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (super.getMinigameName() == null) return;
        if (super.getGame() == null) return;

        if (super.getGame().getCurrentGame() == null) return;
        if (!(super.getGame().getCurrentGame() instanceof MinecartRacing)) return;

        if (super.getGameState() != GameState.IN_GAME)
            e.setCancelled(true);
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        if (super.getMinigameName() == null) return;
        if (super.getGame() == null) return;

        if (super.getGame().getCurrentGame() == null) return;
        if (!(super.getGame().getCurrentGame() instanceof MinecartRacing)) return;

        int playerIndex = super.getGame().getPlayerIndex((Player) e.getExited());
        if (playerIndex == -1) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (super.getMinigameName() == null) return;
        if (super.getGame() == null) return;

        if (super.getGame().getCurrentGame() == null) return;
        if (!(super.getGame().getCurrentGame() instanceof MinecartRacing)) return;

        if (super.getGameState() == GameState.GAME_END) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if (playerIndex == -1) return;
        if (super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        boolean preGame = super.getGameState() != GameState.IN_GAME;
        boolean blockChanged = (int) e.getTo().getX() != (int) e.getFrom().getX()
                || (int) e.getTo().getY() != (int) e.getFrom().getY()
                || (int) e.getTo().getZ() != (int) e.getFrom().getZ();
        if (preGame && blockChanged) {
            e.setTo(e.getFrom());
            return;
        }

        int distanceFromFinishLine = GameUtils.getDistanceFromLocation(e.getPlayer(), this.middle.getX(), this.finishLine.getX(),
                this.middle.getZ(), this.finishLine.getZ());
        super.getGame().getPlayers()[playerIndex].setMinigameScore(distanceFromFinishLine);

        if (axis == 'x' && Math.abs(e.getPlayer().getLocation().getX() - this.finishLine.getX()) < 2
                || axis == 'z' && Math.abs(e.getPlayer().getLocation().getZ() - this.finishLine.getZ()) < 2)
            super.stopMinigame();
    }

    @EventHandler
    public void onShootWool(ProjectileHitEvent e) {
        if (super.getMinigameName() == null) return;
        if (super.getGame() == null) return;

        if (super.getGame().getCurrentGame() == null) return;
        if (!(super.getGame().getCurrentGame() instanceof MinecartRacing)) return;

        if (super.getGameState() != GameState.IN_GAME && super.getGameState() != GameState.GAME_END) return;

        if (e.getEntityType() != EntityType.ARROW) return;

        e.getEntity().remove();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    EntityArrow entityArrow = ((CraftArrow) e.getEntity()).getHandle();

                    Field fieldX = EntityArrow.class.getDeclaredField("d");
                    Field fieldY = EntityArrow.class.getDeclaredField("e");
                    Field fieldZ = EntityArrow.class.getDeclaredField("f");

                    fieldX.setAccessible(true);
                    fieldY.setAccessible(true);
                    fieldZ.setAccessible(true);

                    int x = fieldX.getInt(entityArrow);
                    int y = fieldY.getInt(entityArrow);
                    int z = fieldZ.getInt(entityArrow);

                    if ((x != -1) && (y != -1) && (z != -1)) {
                        Block block = e.getEntity().getWorld().getBlockAt(x, y, z);
                        if (block.getY() <= getMiddle().getY()) return; // If they shoot the rails

                        if (block.getType() != Material.WOOL) return;

                        Wool wool = (Wool) block.getState().getData();
                        CustomPlayer cp = assignedColors.get(SpigotUtils.translateColorToInt(wool.getColor()));
                        if (cp != null) {
                            carts.get(cp.getPlayer().getUniqueId()).setMaxSpeed(carts.get(cp.getPlayer().getUniqueId()).getMaxSpeed() * 1.2);
                            carts.get(cp.getPlayer().getUniqueId()).setVelocity(carts.get(cp.getPlayer().getUniqueId()).getVelocity().multiply(1.2));
                            PacketManager.sendActionbar(cp.getPlayer(), ChatColor.GOLD + "SPEED BOOST");
                        }

                        block.breakNaturally();
                        if (e.getEntity().getShooter() instanceof Player)
                            ((Player) e.getEntity().getShooter()).playSound(((Player) e.getEntity().getShooter()).getLocation(), Sound.SUCCESSFUL_HIT, 1F, 1F);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskLater(super.getGame().getMain(), 1L);
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        if (super.getMinigameName() == null) return;
        if (super.getGame() == null) return;

        if (super.getGame().getCurrentGame() == null) return;
        if (!(super.getGame().getCurrentGame() instanceof MinecartRacing)) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onVehicleUpdate(VehicleUpdateEvent e) {
        if (super.getMinigameName() == null) return;
        if (super.getGame() == null) return;

        if (super.getGame().getCurrentGame() == null) return;
        if (!(super.getGame().getCurrentGame() instanceof MinecartRacing)) return;

        if (super.getGameState() != GameState.IN_GAME && super.getGameState() != GameState.GAME_END) return;

        if (axis == 'x') {
            if (getMiddle().getX() > finishLine.getX()) {
                if (e.getVehicle().getVelocity().getX() > 0)
                    e.getVehicle().setVelocity(e.getVehicle().getVelocity().setX(e.getVehicle().getVelocity().getX() * (-1)));
            } else {
                if (e.getVehicle().getVelocity().getX() < 0)
                    e.getVehicle().setVelocity(e.getVehicle().getVelocity().setX(e.getVehicle().getVelocity().getX() * (-1)));
            }
        } else {
            if (getMiddle().getZ() > finishLine.getZ()) {
                if (e.getVehicle().getVelocity().getZ() > 0)
                    e.getVehicle().setVelocity(e.getVehicle().getVelocity().setZ(e.getVehicle().getVelocity().getZ() * (-1)));
            } else {
                if (e.getVehicle().getVelocity().getZ() < 0)
                    e.getVehicle().setVelocity(e.getVehicle().getVelocity().setZ(e.getVehicle().getVelocity().getZ() * (-1)));
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (super.getMinigameName() == null) return;
        if (super.getGame() == null) return;

        if (super.getGame().getCurrentGame() == null) return;
        if (!(super.getGame().getCurrentGame() instanceof MinecartRacing)) return;

        if (super.getGameState() != GameState.IN_GAME && super.getGameState() != GameState.GAME_END) return;

        if (e.getEntity().getType() == EntityType.DROPPED_ITEM) {
            Item item = (Item) e.getEntity();
            if (item.getItemStack().getType() == Material.WOOL)
                e.getEntity().remove();
        }
    }
}