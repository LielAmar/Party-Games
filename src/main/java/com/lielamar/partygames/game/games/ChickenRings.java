package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.CharValidation;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.entities.custom.ControllableChicken;
import com.lielamar.partygames.modules.objects.Ring;
import com.lielamar.partygames.utils.GameUtils;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChickenRings extends Minigame implements Listener {

    public static Map<UUID, ControllableChicken> chickens = new HashMap<>();

    private static char axis = 'x';
    private static boolean constant_movement = false;

    private Map<CustomPlayer, Integer> lastPassedRing;
    private Map<Integer, Ring> rings;
    private Ring lastRing;

    public ChickenRings(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.axis")) axis = config.getString("parameters.axis").toLowerCase().charAt(0);
        if(config.contains("parameters.constant_movement")) constant_movement = config.getBoolean("parameters.constant_movement");

        this.lastPassedRing = new HashMap<>();
        this.rings = new HashMap<>();

        this.setupChickens();
        this.setupRingsLocations();

        try {
            super.validateVariables(
                    new CharValidation(axis, "[Chicken Rings] Axis must be from the allowed Axes list: x/z", new Character[] { 'x', 'z' })
            );
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupMinigame() {
        CustomPlayer player;
        for(int i = 0; i < getGame().getPlayers().length; i++) {
            if(getGame().getPlayers()[i] == null) continue;

            player = getGame().getPlayers()[i];

            if(axis == 'x') super.initiatePlayer(player, getMiddle().clone().add(0, 0, i));
            else super.initiatePlayer(player, getMiddle().clone().add(i, 0, 0));
        }
    }

    @Override
    public void extraStartParameters() {
        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;

            this.lastPassedRing.put(cp, -1);

            chickens.get(cp.getPlayer().getUniqueId()).getControllableEntityHandler().setCanMove(true);
            chickens.get(cp.getPlayer().getUniqueId()).getControllableEntityHandler().setConstantMovement(constant_movement);
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            super.getGame().getMain().getPacketReader().eject(cp.getPlayer());
        }

        for(ControllableChicken chicken : chickens.values())
            chicken.destroyCustomEntity(chicken);
        chickens = new HashMap<>();
    }


    /**
     * Sets up all chickens & players
     */
    public void setupChickens() {
        new BukkitRunnable() {

            @Override
            public void run() {
                ControllableChicken chicken;

                for(CustomPlayer cp : getGame().getPlayers()) {
                    if(cp == null) continue;

                    getGame().getMain().getPacketReader().inject(cp.getPlayer());

                    chicken = new ControllableChicken(getGame().getMain(), cp.getPlayer().getWorld());
                    chicken.spawnCustomEntity(chicken, cp.getPlayer().getLocation());
                    if(cp.getPlayer().isSneaking())
                        cp.getPlayer().setSneaking(false);

                    chicken.getBukkitEntity().setPassenger(cp.getPlayer());
                    chickens.put(cp.getPlayer().getUniqueId(), chicken);
                }
            }
        }.runTaskLater(super.getGame().getMain(), 4L);
    }

    /**
     * Sets up all rings
     */
    public void setupRingsLocations() {
        Location[] ringLocations = SpigotUtils.fetchLocations(super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + getMinigameName()).getConfig(), "rings");

        for(int i = 0; i < ringLocations.length; i++) {
            this.rings.put(i, new Ring(ringLocations[i], axis));

            if(i == ringLocations.length-1) this.lastRing = this.rings.get(i);
        }
    }

    /**
     * Calculates whether or not the player passed their current ring successfully.
     *
     * @param player   Player to check
     */
    public void calculateRing(CustomPlayer player) {
        if(player == null) return;

        int distanceFromFinishLine = GameUtils.getDistanceFromLocation(player.getPlayer(), this.middle.getX(), this.lastRing.getMiddle().getX(),
                this.middle.getZ(), this.lastRing.getMiddle().getZ());
        player.setMinigameScore(distanceFromFinishLine);

        int playersLastRingId = this.lastPassedRing.get(player);
        int playersNextRingId = playersLastRingId+1;
        Ring nextRing = this.rings.get(playersNextRingId);

        // If the player finished the course
        if(nextRing == null || playersNextRingId == this.rings.size()) {
            super.finishPlayer(player, player.getPlayer().getDisplayName() + "" + ChatColor.YELLOW + " has reached the finish line!");
            return;
        }

        // If the distance from player to nextRing is greater than 0.3, we don't need to check anything.
        if(axis == 'x')
            if(Math.abs(player.getPlayer().getLocation().getX()-nextRing.getMiddle().getX()) > 0.5) return;
            else if(axis == 'z')
                if(Math.abs(player.getPlayer().getLocation().getZ()-nextRing.getMiddle().getZ()) > 0.5) return;

        calculateNextRing(nextRing, player, playersLastRingId);
    }

    /**
     * Calculates the next ring of a player (Whether or not they managed to pass it successfully)
     *
     * @param nextRing            Ring to check
     * @param player              Player to check
     * @param playersLastRingId   ID of the player's last ring
     */
    @SuppressWarnings("deprecation")
    public void calculateNextRing(Ring nextRing, CustomPlayer player, int playersLastRingId) {
        if(nextRing != this.lastRing) {
            if(player.getPlayer().getLocation().distance(nextRing.getMiddle()) > 3) { // If didn't pass in ring
                for(Block block : nextRing.getBlocks())
                    player.getPlayer().sendBlockChange(block.getLocation(), Material.WOOL, (byte) 14); // Sets the ring red

                chickens.get(player.getPlayer().getUniqueId()).getControllableEntityHandler().setSpeed(1);  // Slows down the chicken
                // TODO: particles
            } else {
                for (Block block : nextRing.getBlocks())
                    player.getPlayer().sendBlockChange(block.getLocation(), Material.WOOL, (byte) 5);  // Sets the ring green

                chickens.get(player.getPlayer().getUniqueId()).getControllableEntityHandler().setSpeed(
                        chickens.get(player.getPlayer().getUniqueId()).getControllableEntityHandler().getSpeed() + 0.02);  // Speeds up the chicken
                // TODO: particles
            }
        }

        lastPassedRing.put(player, playersLastRingId+1);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof ChickenRings)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(super.getFinishedPlayers()[0] == super.getGame().getPlayers()[playerIndex]
                || super.getFinishedPlayers()[1] == super.getGame().getPlayers()[playerIndex]
                || super.getFinishedPlayers()[2] == super.getGame().getPlayers()[playerIndex]) return;

        if(this.lastPassedRing == null || this.lastPassedRing.get(super.getGame().getPlayers()[playerIndex]) == null) return;

        int playersLastRingId = this.lastPassedRing.get(super.getGame().getPlayers()[playerIndex]);
        int playersNextRingId = playersLastRingId+1;
        Ring nextRing = this.rings.get(playersNextRingId);

        if(playersLastRingId == -1) playersLastRingId = 0;

        // If they get far away, mark the ring as red
        if(e.getTo().distance(this.rings.get(playersLastRingId).getMiddle()) > 30
                || ChickenRings.chickens.get(e.getPlayer().getUniqueId()).getBukkitEntity().getLocation().distance(this.rings.get(playersLastRingId).getMiddle()) > 30) {

            if(nextRing != this.lastRing)
                calculateNextRing(nextRing, super.getGame().getPlayers()[playerIndex], playersLastRingId);
            return;
        }

        // calculate their next ring if they're ok
        if(!super.getGame().getPlayers()[playerIndex].isSpectator())
            this.calculateRing(super.getGame().getPlayers()[playerIndex]);
    }
}