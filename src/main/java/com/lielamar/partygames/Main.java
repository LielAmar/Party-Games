package com.lielamar.partygames;

import com.lielamar.lielsutils.files.FileManager;
import com.lielamar.lielsutils.map.MapManager;
import com.lielamar.lielsutils.scoreboard.ScoreboardManager;
import com.lielamar.partygames.commands.PartyGames;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.listeners.*;
import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.models.entities.*;
import com.lielamar.partygames.utils.Parameters;
import com.packetmanager.lielamar.PacketManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class Main extends JavaPlugin {

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
        registerCommands();
        registerListeners();
        registerCustomEntities();

        if(Bukkit.getOnlinePlayers().size() > 0)
            initiate();
    }

    @Override
    public void onDisable() {
        destroy();
    }


    public void registerCommands() {
        new PartyGames(this);
    }

    public void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new OnPlayerJoin(this), this);
        pm.registerEvents(new OnPlayerQuit(this), this);
        pm.registerEvents(new OnPlayerDeath(this), this);
//        pm.registerEvents(new OnPlayerInteract(), this); // TODO: remove after testing

        pm.registerEvents(new OnHungerChange(), this);
        pm.registerEvents(new OnItemDrop(), this);
        pm.registerEvents(new OnItemPickup(), this);
        pm.registerEvents(new OnEntitySpawn(), this);
        pm.registerEvents(new OnDamage(), this);
        pm.registerEvents(new OnBlockBreak(), this);
        pm.registerEvents(new OnBlockPlace(), this);
        pm.registerEvents(new OnDurabilityChange(), this);
        pm.registerEvents(new OnWeatherChange(), this);
        pm.registerEvents(new OnPlayerAchievement(), this);
        pm.registerEvents(new OnPlayerFinishMinigame(), this);
        pm.registerEvents(new OnInventoryClick(), this);

        pm.registerEvents(new OnPlayerWinsMinigame(this), this);
    }

    /**
     * Registers all game managers (MapManager, FileManager, ScoreboardManager, PacketReader and game itself)
     */
    public void registerManagers() {
        Parameters.initiate(getConfig());

        this.mapManager = new MapManager(this).saveAllMaps();
        this.fileManager = new FileManager(this);
        this.scoreboardManager = new ScoreboardManager(this, "PARTY GAMES");
        this.controllableEntitiesPacketReader = new ControllableEntitiesPacketReader();

        this.game = new Game(this);
    }

    /**
     * Registers all custom entities for the different minigames
     */
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

    /**
     * Initiates the game
     */
    public void initiate() {
        for(Player pl : Bukkit.getOnlinePlayers()) {
            scoreboardManager.injectPlayer(pl);
            game.addPlayer(pl, false);
        }
    }

    /**
     * Destroys the game
     */
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
    }


    public FileManager getFileManager() { return this.fileManager; }
    public ScoreboardManager getScoreboardManager() { return this.scoreboardManager; }
    public ControllableEntitiesPacketReader getPacketReader() { return this.controllableEntitiesPacketReader; }

    public Game getGame() { return this.game; }
}