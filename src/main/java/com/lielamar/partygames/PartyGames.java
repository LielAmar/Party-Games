package com.lielamar.partygames;

import com.lielamar.lielsutils.files.FileManager;
import com.lielamar.lielsutils.map.MapManager;
import com.lielamar.lielsutils.scoreboard.ScoreboardManager;
import com.lielamar.packetmanager.PacketManager;
import com.lielamar.partygames.commands.CommandManager;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.listeners.*;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.entities.ControllableEntitiesPacketReader;
import com.lielamar.partygames.modules.entities.custom.*;
import com.lielamar.partygames.utils.Parameters;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class PartyGames extends JavaPlugin {

    public static final Random rnd = new Random();

    private MapManager mapManager;
    private FileManager fileManager;
    private ScoreboardManager scoreboardManager;
    private ControllableEntitiesPacketReader controllableEntitiesPacketReader;

    private Game game;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registerManagers();
        registerListeners();
        registerCustomEntities();

        initiate();
    }

    public void registerManagers() {
        Parameters.initiate(getConfig());

        new CommandManager(this);
        this.mapManager = new MapManager(this).saveAllMaps();
        this.fileManager = new FileManager(this);
        this.scoreboardManager = new ScoreboardManager(this, "PARTY GAMES");
        this.controllableEntitiesPacketReader = new ControllableEntitiesPacketReader();

        this.game = new Game(this);
    }

    public void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new OnPlayerJoin(this), this);
        pm.registerEvents(new OnPlayerQuit(this), this);
        pm.registerEvents(new OnPlayerDeath(this), this);

        pm.registerEvents(new PlayerEventsHandler("partygames.admin"), this);

        pm.registerEvents(new OnEntitySpawn(), this);
        pm.registerEvents(new OnWeatherChange(), this);

        pm.registerEvents(new OnPlayerFinishMinigame(), this);
        pm.registerEvents(new OnPlayerWinsMinigame(this), this);
    }

    public void registerCustomEntities() {
        PacketManager.registerEntity("controllablechicken", 93, EntityChicken.class, ControllableChicken.class);
        PacketManager.registerEntity("controllablecow", 92, EntityCow.class, ControllableCow.class);
        PacketManager.registerEntity("controllablepig", 90, EntityPig.class, ControllablePig.class);
        PacketManager.registerEntity("controllablesheep", 91, EntitySheep.class, ControllableSheep.class);
        PacketManager.registerEntity("shootingrangezombie", 54, EntityZombie.class, ShootingRangeZombie.class);
        PacketManager.registerEntity("shootingrangeskeleton", 51, EntitySkeleton.class, ShootingRangeSkeleton.class);
        PacketManager.registerEntity("chasingspider", 52, EntitySpider.class, ChasingSpider.class);
        PacketManager.registerEntity("customfallingblock", 21, EntityFallingBlock.class, CustomFallingBlock.class);
        PacketManager.registerEntity("workshopkeeper", 120, EntityVillager.class, WorkshopKeeper.class);
    }

    public void initiate() {
        Bukkit.getOnlinePlayers().forEach(pl -> {
            scoreboardManager.injectPlayer(pl);
            game.addPlayer(pl, false);
        });
    }

    @Override
    public void onDisable() {
        destroy();
    }

    public void destroy() {
        this.mapManager.restoreAllMaps();

        for(Player pl : Bukkit.getOnlinePlayers())
            controllableEntitiesPacketReader.eject(pl);

        if(game != null && game.getPlayers() != null) {
            for(CustomPlayer cp : game.getPlayers()) {
                if(cp == null) continue;
                cp.destroyBossBar();
            }
        }

        this.fileManager = null;
        this.scoreboardManager = null;
        this.controllableEntitiesPacketReader = null;

        this.game = null;
        System.gc();
    }


    public FileManager getFileManager() { return this.fileManager; }
    public ScoreboardManager getScoreboardManager() { return this.scoreboardManager; }
    public ControllableEntitiesPacketReader getPacketReader() { return this.controllableEntitiesPacketReader; }

    public Game getGame() { return this.game; }
}