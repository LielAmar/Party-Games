package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.modules.Node;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.PartyGames;
import com.lielamar.partygames.game.*;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.entities.custom.ControllableSheep;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.utils.GameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuperSheep extends Minigame implements Listener {

    public static Map<UUID, ControllableSheep> sheeps = new HashMap<>();

    private static boolean constant_movement = false;
    private static Integer[] wool_colors = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 14 };
    private static int start_length = 10;

    private Map<Integer, CustomPlayer> assignedColors;
    private Map<UUID, Node<Location>> nodes;
    private int current_length;

    public SuperSheep(Game game, GameType gameType) {
        super(game, gameType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        if(config.contains("parameters.constant_movement")) constant_movement = config.getBoolean("parameters.constant_movement");
        if(config.contains("parameters.start_length")) start_length = config.getInt("parameters.start_length");
        if(config.contains("parameters.wool_colors") && config.isList("parameters.wool_colors"))
            wool_colors = config.getIntegerList("parameters.wool_colors").toArray(new Integer[0]);

        this.assignedColors = new HashMap<>();
        this.nodes = new HashMap<>();
        this.current_length = start_length + PartyGames.rnd.nextInt(5)-2;

        GameUtils.assignColorsToPlayers(super.getGame().getPlayers(), wool_colors, this.assignedColors);
        this.setupSheeps();

        try {
            super.validateVariables(
                    new IntValidation(start_length, "[Super Sheep] Start Length must be greater than 0", 0),
                    new IntValidation(wool_colors.length, "[Super Sheep] Amount of Colors must be greater than/equals to Amount Of Players", super.getGame().getPlayers().length));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        this.startAdditionalTimer();

        for(ControllableSheep sheep : sheeps.values()) {
            if(sheep != null) {
                sheep.getControllableEntityHandler().setCanMove(true);
                sheep.getControllableEntityHandler().setConstantMovement(constant_movement);
            }
        }

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            cp.getPlayer().setExp(1F);

            nodes.put(cp.getPlayer().getUniqueId(), new Node<>(cp.getPlayer().getLocation()));
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            super.getGame().getMain().getPacketReader().eject(cp.getPlayer());
        }

        for(ControllableSheep sheep : sheeps.values())
            sheep.destroyCustomEntity(sheep);
        sheeps = new HashMap<>();
    }


    /**
     * An additional timer running alongside the main minigame timer (async)
     * Adding exp to players so they can use the boost feature
     */
    public void startAdditionalTimer() {
        new BukkitRunnable() {
            int i = 4*getGameType().getGameDuration();

            @Override
            public void run() {
                if(i == 0 || getGameState() == GameState.GAME_END) {
                    this.cancel();
                    return;
                }

                if(i%30 == 0)
                    current_length++;

                for(CustomPlayer cp : getGame().getPlayers()) {
                    if(cp == null) continue;
                    if(cp.getPlayer().getExp() > 1F)
                        cp.getPlayer().setExp(1F);
                    else
                        cp.getPlayer().setExp(cp.getPlayer().getExp() + 0.05F);
                }

                i--;
            }
        }.runTaskTimerAsynchronously(super.getGame().getMain(), 0L, 5L);
    }

    /**
     * Sets up all sheeps & players
     */
    public void setupSheeps() {
        new BukkitRunnable() {

            @Override
            public void run() {
                ControllableSheep sheep;

                for(CustomPlayer cp : getGame().getPlayers()) {
                    if(cp == null) continue;

                    getGame().getMain().getPacketReader().inject(cp.getPlayer());

                    sheep = new ControllableSheep(getGame().getMain(), cp.getPlayer().getWorld());
                    sheep.spawnCustomEntity(sheep, cp.getPlayer().getLocation());
                    if(cp.getPlayer().isSneaking())
                        cp.getPlayer().setSneaking(false);

                    sheep.getBukkitEntity().setPassenger(cp.getPlayer());
                    ((Sheep)sheep.getBukkitEntity()).setColor(SpigotUtils.translateIntToColor(GameUtils.getPlayerAssignedColor(cp, assignedColors)));

                    sheeps.put(cp.getPlayer().getUniqueId(), sheep);
                }
            }
        }.runTaskLater(super.getGame().getMain(), 3L);
    }

    /**
     * Shifts all nodes to the previous one
     *
     * @param cp         {@link CustomPlayer} who we shift nodes to
     * @param location   Location we add
     */
    public void shiftNodes(CustomPlayer cp, Location location) {
        Node<Location> first = null;

        if(!nodes.containsKey(cp.getPlayer().getUniqueId()) || nodes.get(cp.getPlayer().getUniqueId()) == null) {
            first = new Node<>(location);
            nodes.put(cp.getPlayer().getUniqueId(), first);
        }
        if(first == null)
            first = nodes.get(cp.getPlayer().getUniqueId());

        int amountOfNodes = Node.amountOfNodes(first);
        int playersColor = GameUtils.getPlayerAssignedColor(cp, this.assignedColors);

        // Shifting all nodes. Example (-1 = new location):
        // [0] -> [1] -> [2] -> [3]   =>   [-1] -> [0] -> [1] -> [2]
        //     previousValue   |   first   |   newValue
        //           -         |     -     |      -1
        //           0         |     -1    |       0
        //           1         |      0    |       1
        //           2         |      1    |       2
        Location newValue = location;
        while(first.getNext() != null) {
            Location previousValue = first.getValue();
            first.setValue(newValue);
            newValue = previousValue;

            first = first.getNext();
        }

        // We also set the value of the last node
        Node<Location> last = first;
        Location previousValue = last.getValue();
        last.setValue(newValue);
        newValue = previousValue;

        // If we have less nodes than how many we should have, we add a new node with the
        // newValue (from the previous while loop) and set it as the last node.
        // If our amount of nodes is equal/greater than the current length, we simply change the
        // newValue's block type to Air since it's the location of the previous last node
        newValue.getBlock().setType(Material.AIR);
        if(amountOfNodes < current_length) {
            Node<Location> newLastNode = new Node<>(newValue);
            last.setNext(newLastNode);
        }

        first = nodes.get(cp.getPlayer().getUniqueId());

        // Looping through all of the list, updating all blocks.
        while(first != null) {
            updateBlock(first.getValue().getBlock(), playersColor);
            first = first.getNext();
        }
    }

    /**
     * Updates the given block with the given color
     *
     * @param block   Block to update
     * @param color   Color to set
     */
    @SuppressWarnings("deprecation")
    public void updateBlock(Block block, int color) {
        block.setType(Material.STAINED_GLASS_PANE);
        block.setData((byte)color);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof SuperSheep)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(super.getGameState() == GameState.IN_GAME) {
            if((int)e.getTo().getX() != (int)e.getFrom().getX()
                    || (int)e.getTo().getY() != (int)e.getFrom().getY()
                    || (int)e.getTo().getZ() != (int)e.getFrom().getZ()) {

                int xChange = e.getTo().getBlockX() - e.getFrom().getBlockX();
                int zChange = e.getTo().getBlockZ() - e.getFrom().getBlockZ();

                // If the player get stuck in a wall/other snake, we need to turn them to a spectator
                if(e.getTo().getBlock().getType() != Material.AIR || e.getTo().clone().add(xChange, 0, zChange).getBlock().getType() != Material.AIR) {
                    initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
                    sheeps.get(e.getPlayer().getUniqueId()).getBukkitEntity().remove();
                } else
                    shiftNodes(super.getGame().getPlayers()[playerIndex], e.getFrom());
            }
        }
    }
}