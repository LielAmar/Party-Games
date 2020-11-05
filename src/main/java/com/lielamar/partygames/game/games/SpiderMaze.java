package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.entities.custom.ChasingSpider;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpiderMaze extends Minigame implements Listener {

    public static Map<UUID, ChasingSpider> spiders = new HashMap<>();

    private Location finishpointStart, finishpointEnd;
    private List<Block> finishpointBlocks;

    public SpiderMaze(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
        super.middle.getWorld().setTime(14000);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + getMinigameName()).getConfig();

        this.setupFinishPoint(config);
    }

    @Override
    public void extraStartParameters() {
        new BukkitRunnable() {

            @Override
            public void run() {
                for(int i = 0; i < getGame().getPlayers().length; i++) {
                    if(getGame().getPlayers()[i] == null) continue;

                    ChasingSpider spider = new ChasingSpider(getMiddle().getWorld(), getGame().getPlayers()[i].getPlayer());
                    spider.spawnCustomEntity(spider, getLocations()[i]);
                    spiders.put(getGame().getPlayers()[i].getPlayer().getUniqueId(), spider);
                }
            }
        }.runTaskLater(super.getGame().getMain(), 10L);
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(UUID u : spiders.keySet()) {
            if(u == null) continue;
            Player p = Bukkit.getPlayer(u);
            if(p == null) continue;

            super.getGame().getMain().getPacketReader().eject(p);
            if(spiders == null) return;
            if(spiders.containsKey(p.getUniqueId()))
                spiders.get(p.getUniqueId()).getWorld().removeEntity(spiders.get(p.getUniqueId()));
        }
    }


    /**
     * Sets up the finish point
     *
     * @param config   Config to load data from
     */
    public void setupFinishPoint(YamlConfiguration config) {
        this.finishpointStart = SpigotUtils.fetchLocation(config, "finishpoint.start");
        this.finishpointEnd = SpigotUtils.fetchLocation(config, "finishpoint.end");
        this.finishpointBlocks = new ArrayList<>();

        for(int x = 0; Math.abs(x) <= Math.abs(this.finishpointStart.getX()-this.finishpointEnd.getX()); x+= (this.finishpointStart.getX() > this.finishpointEnd.getX()) ? -1 : 1) {
            for(int y = 0; Math.abs(y) <= Math.abs(this.finishpointStart.getY()-this.finishpointEnd.getY()); y+= (this.finishpointStart.getY() > this.finishpointEnd.getY()) ? -1 : 1) {
                for(int z = 0; Math.abs(z) <= Math.abs(this.finishpointStart.getZ()-this.finishpointEnd.getZ()); z+= (this.finishpointStart.getZ() > this.finishpointEnd.getZ()) ? -1 : 1)
                    finishpointBlocks.add(this.finishpointStart.clone().add(x, y, z).getBlock());
            }
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof SpiderMaze)) return;

        if(super.getGameState() == GameState.GAME_END) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        boolean preGame = super.getGameState() != GameState.IN_GAME;
        boolean blockChanged = (int)e.getTo().getX() != (int)e.getFrom().getX()
                || (int)e.getTo().getY() != (int)e.getFrom().getY()
                || (int)e.getTo().getZ() != (int)e.getFrom().getZ();
        if(preGame && blockChanged) e.setTo(e.getFrom());
    }

    @EventHandler
    public void onPlayerPhysics(PlayerInteractEvent e) {
        if(e.getAction() != Action.PHYSICAL) return;
        if(e.getClickedBlock().getType() != Material.WOOD_PLATE) return;

        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof SpiderMaze)) return;

        e.setCancelled(true);

        if(super.getGameState() != GameState.IN_GAME) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(finishpointBlocks.contains(e.getClickedBlock())) {
            super.finishPlayer(super.getGame().getPlayers()[playerIndex], e.getPlayer().getDisplayName() + "" + ChatColor.YELLOW + " has finished the parkour!");
            super.initiateSpectator(super.getGame().getPlayers()[playerIndex], false, true, 0);

            if(spiders.containsKey(e.getPlayer().getUniqueId()))
                spiders.get(e.getPlayer().getUniqueId()).getWorld().removeEntity(spiders.get(e.getPlayer().getUniqueId()));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof SpiderMaze)) return;

        e.setCancelled(true);

        if(!(e.getEntity() instanceof Player)) return;
        if(e.getDamager().getType() != EntityType.SPIDER) return;

        int playerIndex = super.getGame().getPlayerIndex(((Player)e.getEntity()));
        if(playerIndex == -1) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        if(super.getGame().getPlayers()[playerIndex].isSpectator()) return;

        e.setCancelled(false);
        e.setDamage(4);

        Player p = (Player)e.getEntity();

        if(p.getHealth()-e.getDamage() <= 0) {
            e.setCancelled(true);
            p.setHealth(20);

            super.finishPlayer(super.getGame().getPlayers()[playerIndex], p.getDisplayName() + "" + ChatColor.YELLOW + " has finished the parkour!");
            super.initiateSpectator(super.getGame().getPlayers()[playerIndex], false, true, 0);

            super.getGame().infoPlayers(ChatColor.RED + p.getName() + ChatColor.YELLOW + " has been killed by a " + ChatColor.GREEN + "Spider" + ChatColor.YELLOW + "!");

            if(spiders.containsKey(p.getUniqueId())) {
                spiders.get(p.getUniqueId()).getWorld().removeEntity(spiders.get(p.getUniqueId()));
                spiders.remove(p.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        if(super.getMinigameName() == null) return;
        if(super.getGame() == null) return;

        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof SpiderMaze)) return;

        if(!(e.getEntity() instanceof Player)) return;

        e.setCancelled(true);
    }
}