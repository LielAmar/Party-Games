package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.utils.GameUtils;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TheFloorIsLava extends Minigame implements Listener {

    private static double minimum_y = 95.0;

    private List<UUID> checkpointPlayers;
    private List<Block> checkpointBlocks, endpointBlocks;
    private Location wallStart, wallEnd;
    private Location checkpoint, checkpointStart, checkpointEnd;
    private Location endpointStart, endpointEnd;

    public TheFloorIsLava(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        super.middle.getWorld().setTime(14000);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + getMinigameName()).getConfig();

        if(config.contains("parameters.minimum_y")) minimum_y = config.getDouble("parameters.minimum_y");

        this.checkpointPlayers = new ArrayList<>();

        this.setupPoints(config);
    }

    @Override
    public void setupMinigame() {
        super.setupMinigame();

        for(int i = 0; i < super.getGame().getPlayers().length; i++) {
            if(super.getGame().getPlayers()[i] == null) continue;
            super.getGame().getPlayers()[i].getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 4));

            for(CustomPlayer cp : super.getGame().getPlayers()) {
                if(cp == null) continue;
                super.getGame().getPlayers()[i].getPlayer().showPlayer(cp.getPlayer());
            }
        }
    }

    @Override
    public void extraStartParameters() {
        for(int x = 0; Math.abs(x) <= Math.abs(this.wallStart.getX()-this.wallEnd.getX()); x+= (this.wallStart.getX() > this.wallEnd.getX()) ? -1 : 1) {
            for(int y = 0; Math.abs(y) <= Math.abs(this.wallStart.getY()-this.wallEnd.getY()); y+= (this.wallStart.getY() > this.wallEnd.getY()) ? -1 : 1) {
                for(int z = 0; Math.abs(z) <= Math.abs(this.wallStart.getZ()-this.wallEnd.getZ()); z+= (this.wallStart.getZ() > this.wallEnd.getZ()) ? -1 : 1) {
                    this.wallStart.clone().add(x, y, z).getBlock().setType(Material.AIR);
                }
            }
        }

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            for(CustomPlayer cp2 : super.getGame().getPlayers()) {
                if(cp2 == null) continue;
                if(cp == cp2) continue;
                cp.getPlayer().hidePlayer(cp2.getPlayer());
            }
        }
    }


    /**
     * Sets up all points (wall, checkpoint, endpoint)
     *
     * @param config   Config to load data from
     */
    public void setupPoints(YamlConfiguration config) {
        this.wallStart = SpigotUtils.fetchLocation(config, "wall.start");
        this.wallEnd = SpigotUtils.fetchLocation(config, "wall.end");

        this.checkpointStart = SpigotUtils.fetchLocation(config, "checkpoint.start");
        this.checkpointEnd = SpigotUtils.fetchLocation(config, "checkpoint.end");
        this.checkpointBlocks = new ArrayList<>();

        this.endpointStart = SpigotUtils.fetchLocation(config, "endpoint.start");
        this.endpointEnd = SpigotUtils.fetchLocation(config, "endpoint.end");
        this.endpointBlocks = new ArrayList<>();

        for(int x = 0; Math.abs(x) <= Math.abs(this.checkpointStart.getX()-this.checkpointEnd.getX()); x+= (this.checkpointStart.getX() > this.checkpointEnd.getX()) ? -1 : 1) {
            for(int y = 0; Math.abs(y) <= Math.abs(this.checkpointStart.getY()-this.checkpointEnd.getY()); y+= (this.checkpointStart.getY() > this.checkpointEnd.getY()) ? -1 : 1) {
                for(int z = 0; Math.abs(z) <= Math.abs(this.checkpointStart.getZ()-this.checkpointEnd.getZ()); z+= (this.checkpointStart.getZ() > this.checkpointEnd.getZ()) ? -1 : 1)
                    checkpointBlocks.add(this.checkpointStart.clone().add(x, y, z).getBlock());
            }
        }

        for(int x = 0; Math.abs(x) <= Math.abs(this.endpointStart.getX()-this.endpointEnd.getX()); x+= (this.endpointStart.getX() > this.endpointEnd.getX()) ? -1 : 1) {
            for(int y = 0; Math.abs(y) <= Math.abs(this.endpointStart.getY()-this.endpointEnd.getY()); y+= (this.endpointStart.getY() > this.endpointEnd.getY()) ? -1 : 1) {
                for(int z = 0; Math.abs(z) <= Math.abs(this.endpointStart.getZ()-this.endpointEnd.getZ()); z+= (this.endpointStart.getZ() > this.endpointEnd.getZ()) ? -1 : 1)
                    endpointBlocks.add(this.endpointStart.clone().add(x, y, z).getBlock());
            }
        }

        this.checkpoint = new Location(super.getMiddle().getWorld(), checkpointBlocks.get(checkpointBlocks.size()/2).getX(),
                checkpointBlocks.get(checkpointBlocks.size()/2).getY(), checkpointBlocks.get(checkpointBlocks.size()/2).getZ(), super.getMiddle().getYaw(), super.getMiddle().getPitch());
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof TheFloorIsLava)) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()
                || super.getFinishedPlayers()[0] == super.getGame().getPlayers()[playerIndex]
                || super.getFinishedPlayers()[1] == super.getGame().getPlayers()[playerIndex]
                || super.getFinishedPlayers()[2] == super.getGame().getPlayers()[playerIndex]) return;

        if(e.getPlayer().getLocation().getY() < minimum_y) {
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.FALL_BIG, 1F, 1F);
            if(this.checkpointPlayers.contains(e.getPlayer().getUniqueId()))
                e.getPlayer().teleport(checkpoint);
            else
                e.getPlayer().teleport(this.getMiddle());
            e.getPlayer().setFireTicks(0);
            return;
        }

        int distanceFromFinishLine = GameUtils.getDistanceFromLocation(e.getPlayer(),
                this.middle.getX(), this.endpointBlocks.get(this.endpointBlocks.size()/2).getX(),
                this.middle.getZ(), this.endpointBlocks.get(this.endpointBlocks.size()/2).getZ());
        super.getGame().getPlayers()[playerIndex].setMinigameScore(distanceFromFinishLine);
    }

    @EventHandler
    public void onPlayerPhysics(PlayerInteractEvent e) {
        if(e.getAction() != Action.PHYSICAL) return;
        if(e.getClickedBlock().getType() != Material.STONE_PLATE) return;

        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof TheFloorIsLava)) return;

        e.setCancelled(true);

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        if(super.finishedPlayers[0] == super.getGame().getPlayers()[playerIndex]
                || super.finishedPlayers[1] == super.getGame().getPlayers()[playerIndex]
                || super.finishedPlayers[2] == super.getGame().getPlayers()[playerIndex]) return;

        if(checkpointBlocks.contains(e.getClickedBlock()) && !checkpointPlayers.contains(e.getPlayer().getUniqueId())) {
            checkpointPlayers.add(e.getPlayer().getUniqueId());
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.SUCCESSFUL_HIT, 1F, 1F);
        } else if(endpointBlocks.contains(e.getClickedBlock())) {
            super.finishPlayer(super.getGame().getPlayers()[playerIndex], e.getPlayer().getDisplayName() + "" + ChatColor.YELLOW + " has finished the parkour!");
            super.initiateSpectator(super.getGame().getPlayers()[playerIndex], false, true, 0);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.SUCCESSFUL_HIT, 1F, 1F);
        }
    }
}