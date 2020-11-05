package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

public class Volcano extends Minigame implements Listener {

    private static int radius = 15;
    private static double minimum_y = 90.0;
    private static int random_break_delay = 3;

    public Volcano(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.radius")) radius = config.getInt("parameters.radius");
        if(config.contains("parameters.minimum_y")) minimum_y = config.getDouble("parameters.minimum_y");
        if(config.contains("parameters.random_break_delay")) random_break_delay = config.getInt("parameters.random_break_delay");

        try {
            super.validateVariables(
                    new IntValidation(radius, "[Volcano] Radius must be greater than 0", 1),
                    new IntValidation(random_break_delay, "[Volcano] Random Break Delay must be greater than 0", 1));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        this.startAdditionalTimer();

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) return;

            Block b = cp.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
            fadeBlock(b);
        }
    }


    /**
     * An additional timer running alongside the main minigame timer (async)
     * Adding exp to players so they can use the boost feature
     */
    public void startAdditionalTimer() {
        final int yFixer;
        if(middle.clone().add(0, -1, 0).getBlock().getType() == Material.WOOL)
            yFixer = -1;
        else if(middle.clone().add(0, 1, 0).getBlock().getType() == Material.WOOL)
            yFixer = 1;
        else
            yFixer = 0;

        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {

                if(i >= getGameTime()) {
                    this.cancel();
                    return;
                }

                if(middle != null) {
                    for(int amount = 0; amount < i / random_break_delay; amount++) {

                        Block b;
                        int x = Main.rnd.nextInt(radius * 2) - radius;
                        int z = Main.rnd.nextInt(radius * 2) - radius;
                        b = middle.clone().add(x, yFixer, z).getBlock();

                        if(b == null) continue;

                        if(b.getType() != Material.WOOL)
                            continue;

                        fadeBlock(b);
                    }
                }

                for(CustomPlayer cp : getGame().getPlayers()) {
                    if(cp == null) continue;
                    if(cp.isSpectator()) continue;

                    Block b = cp.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
                    if(b == null) continue;
                    if(b.getType() == Material.AIR) {
                        fadeBlock(b.getRelative(BlockFace.NORTH));
                        fadeBlock(b.getRelative(BlockFace.SOUTH));
                        fadeBlock(b.getRelative(BlockFace.EAST));
                        fadeBlock(b.getRelative(BlockFace.WEST));
                    }
                }

                i++;
            }
        }.runTaskTimer(super.getGame().getMain(), 0, 20L);
    }

    /**
     * Handles block fade
     *
     * @param block   Block to fade
     */
    @SuppressWarnings("deprecation")
    public void fadeBlock(Block block) {
        new BukkitRunnable() {

            @Override
            public void run() {
                if(getGameState() != GameState.IN_GAME) {
                    this.cancel();
                    return;
                }

                if(block.getType() == Material.AIR)
                    this.cancel();
                else if(block.getType() == Material.WOOL) {
                    if(SpigotUtils.translateColorToInt(((Wool)block.getState().getData()).getColor()) == 14) {
                        block.setType(Material.AIR);
                        this.cancel();
                    } else if(SpigotUtils.translateColorToInt(((Wool)block.getState().getData()).getColor()) == 1) {
                        block.setData((byte)14, true);
                    } else if(SpigotUtils.translateColorToInt(((Wool)block.getState().getData()).getColor()) == 4) {
                        block.setData((byte)1, true);
                    } else if(SpigotUtils.translateColorToInt(((Wool)block.getState().getData()).getColor()) == 8) {
                        block.setData((byte)4, true);
                    } else {
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(super.getGame().getMain(), 0L, (50 + Main.rnd.nextInt(21)));
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Volcano)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        if(super.getGameState() == GameState.IN_GAME) {

            if(e.getTo().getY() < minimum_y) {
                initiateSpectator(super.getGame().getPlayers()[playerIndex], true, true, 1);
                return;
            }

            if((int)e.getTo().getX() != (int)e.getFrom().getX()
                    || (int)e.getTo().getY() != (int)e.getFrom().getY()
                    || (int)e.getTo().getZ() != (int)e.getFrom().getZ()) {
                Block b = e.getTo().getBlock().getRelative(BlockFace.DOWN);

                fadeBlock(b);
            }
        }
    }
}