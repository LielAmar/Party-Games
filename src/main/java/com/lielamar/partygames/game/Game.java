package com.lielamar.partygames.game;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.TextUtils;
import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.lielsutils.scoreboard.ScoreboardManager;
import com.lielamar.lielsutils.scoreboard.ScoreboardUtils;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.games.*;
import com.lielamar.partygames.events.GameEndEvent;
import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.utils.GameUtils;
import com.lielamar.partygames.utils.Parameters;
import com.packetmanager.lielamar.PacketManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class Game {

    private Main main;
    private GameState state;
    private int remainingTime;

    private Location lobby;

    private CustomPlayer[] players;
    private GameType[] minigames;

    private Minigame currentGame;
    private int currentGameID;

    private List<String> waitscoreboard;
    private List<String> countdownscoreboard;
    private List<String> gamestatsscoreboard;

    private final Scoreboard spectatorsScoreboard;
    private final Team spectatorsTeam;
    private final Scoreboard staffScoreboard;
    private final Team staffTeam;

    public Game(Main main) {
        this.main = main;
        this.state = GameState.WAITING_FOR_PLAYERS;
        this.remainingTime = 0;

        this.lobby = SpigotUtils.fetchLocation(main.getConfig(), "lobby");

        this.players = new CustomPlayer[Parameters.MAXIMUM_PLAYERS()];
        this.generateMinigames();

        this.currentGame = null;
        this.currentGameID = 0;

        this.waitscoreboard = main.getFileManager().getConfig("config").getConfig().getStringList("waitscoreboard");
        this.countdownscoreboard = main.getFileManager().getConfig("config").getConfig().getStringList("countdownscoreboard");
        this.gamestatsscoreboard = main.getFileManager().getConfig("config").getConfig().getStringList("gamestatsscoreboard");

        this.spectatorsScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.spectatorsTeam = this.spectatorsScoreboard.registerNewTeam("Ghost");
        this.spectatorsTeam.setCanSeeFriendlyInvisibles(true);

        this.staffScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.staffTeam = this.staffScoreboard.registerNewTeam("Ghost");
        this.staffTeam.setCanSeeFriendlyInvisibles(true);
    }


    /**
     * Adds a player to the game if theres enough room
     *
     * @param player   Player to add to the game
     * @return         Whether or not the player was added
     */
    public boolean addPlayer(Player player, boolean forceAdd) {
        int amountOfPlayers = this.getAmountOfPlayers();
        boolean isFreeSlot = amountOfPlayers < this.getPlayers().length;

        if(!forceAdd) {
            boolean isInGame = this.getPlayerIndex(player) != -1;
            boolean canJoinState = this.getGameState() == GameState.WAITING_FOR_PLAYERS || this.getGameState() == GameState.COUNTING_DOWN;
            if(isInGame || !isFreeSlot || !canJoinState)
                return false;
        }

        if(!isFreeSlot) {
            CustomPlayer[] tmpPlayers = new CustomPlayer[this.getPlayers().length + (this.getPlayers().length/2) + 1];
            for(int i = 0; i < this.getPlayers().length; i++)
                tmpPlayers[i] = this.getPlayers()[i];
            this.players = tmpPlayers;
        }

        for(int i = 0; i < this.getPlayers().length; i++) {

            CustomPlayer p = this.getPlayers()[i];
            if(p == null || p.getPlayer() == null) {

                this.getPlayers()[i] = new CustomPlayer(player);
                amountOfPlayers++;

                infoPlayers(player.getDisplayName() + ChatColor.YELLOW + " has joined (" + ChatColor.AQUA + amountOfPlayers +
                        ChatColor.YELLOW + "/" + ChatColor.AQUA + Parameters.MAXIMUM_PLAYERS() + ChatColor.YELLOW + ")");

                if(getGameState() != GameState.IN_GAME) {
                    this.fixPlayer(player);
                    if(amountOfPlayers >= Parameters.MINIMUM_PLAYERS()) {
                        if (this.getGameState() != GameState.COUNTING_DOWN)
                            this.startCountdown();
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a player from the game
     *
     * @param player   Player to remove from the game
     * @return         Whether or not the player was removed
     */
    public boolean removePlayer(Player player) {
        boolean isInGame = this.getPlayerIndex(player) != -1;
        if(!isInGame) return false;

        for(int i = 0; i < this.getPlayers().length; i++) {

            CustomPlayer p = this.getPlayers()[i];
            if(p == null) continue;

            if(p.getPlayer() == player) {
                this.getPlayers()[i] = null;
                this.infoPlayers(player.getDisplayName() + ChatColor.YELLOW + " has quit!");

                int amount_of_players = this.getAmountOfPlayers();

                if(amount_of_players < Parameters.MINIMUM_PLAYERS() && this.getGameState() == GameState.COUNTING_DOWN)
                    this.stopCountdown();

                if(amount_of_players == 1 && this.getGameState() == GameState.IN_GAME) {
                    if(this.getCurrentGame() != null) {
                        this.currentGameID = this.minigames.length;
                        this.getCurrentGame().stopMinigame();
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the GameStart Countdown Timer
     */
    public void startCountdown() {
        this.state = GameState.COUNTING_DOWN;
        this.remainingTime = Parameters.COUNTDOWN_TIME();

        new BukkitRunnable() {
            int time = Parameters.COUNTDOWN_TIME();

            @Override
            public void run() {
                if(getGameState() == GameState.WAITING_FOR_PLAYERS) {
                    infoPlayers(ChatColor.YELLOW + "Countdown Stopped!");
                    scoreboardPlayers(ScoreboardUtils.assembleScoreboard(waitscoreboard, new Pair<>("%date%", TextUtils.getDate()),
                            new Pair<>("%map%", "Party Games"),
                            new Pair<>("%players%", getAmountOfPlayers() + ""),
                            new Pair<>("%maxplayers%", Parameters.MAXIMUM_PLAYERS() + ""),
                            new Pair<>("%remainingtime%", TextUtils.formatSeconds(getRemainingTime()) + "")));
                }

                if(getGameState() != GameState.COUNTING_DOWN) {
                    this.cancel();
                    return;
                }

                if(time == 0) {
                    this.cancel();
                    startGame();
                    return;
                }

                if(time <= 5 || time == 10 || time == 15 || time == 30)
                    infoPlayers(ChatColor.YELLOW + "The game starts in " + ChatColor.RED + time + ChatColor.YELLOW + " seconds!");
                if(time <= 3) {
                    titlePlayers(ChatColor.RED + "" + time);
                    playSound(Sound.WOOD_CLICK, 1F, 1F);
                } else if(time <= 5) titlePlayers(ChatColor.GOLD + "" + time);
                else if(time <= 10) titlePlayers(ChatColor.GREEN + "" + time);

                scoreboardPlayers(ScoreboardUtils.assembleScoreboard(countdownscoreboard, new Pair<>("%date%", TextUtils.getDate()),
                        new Pair<>("%map%", "Party Games"),
                        new Pair<>("%players%", getAmountOfPlayers() + ""),
                        new Pair<>("%maxplayers%", Parameters.MAXIMUM_PLAYERS() + ""),
                        new Pair<>("%remainingtime%", TextUtils.formatSeconds(getRemainingTime()) + "")));

                time = time-1;
                remainingTime = getRemainingTime()-1;
            }
        }.runTaskTimer(this.getMain(), 0L, 20L);
    }

    /**
     * Stops the GameStart Countdown Timer
     */
    public void stopCountdown() {
        this.state = GameState.WAITING_FOR_PLAYERS;
    }


    /**
     * Starts The Game
     */
    public void startGame() {
        this.state = GameState.IN_GAME;

        this.actionbarPlayers(ChatColor.YELLOW + "You are playing on LielAmar's Network!");
        this.runNextMinigame();
    }

    /**
     * Generate a list of {@link com.lielamar.partygames.game.GameType} objects for this game instance
     * Loads all minigames upon server start to avoid loading them when the game runs and cause players to lag
     */
    public void generateMinigames() {
        this.minigames = new GameType[Parameters.AMOUNT_OF_GAMES()];

        int amount_of_games = 0;
        while(amount_of_games < Parameters.AMOUNT_OF_GAMES()) {
            GameType type = GameType.values()[Main.rnd.nextInt(GameType.values().length)];
            if(!this.containsMinigame(type)) {
                this.minigames[amount_of_games] = type;
                amount_of_games++;
            }
        }
    }

    /**
     * Runs the next game or ends the game
     */
    public void runNextMinigame() {
        if(this.getCurrentGameId() >= this.getMinigames().length) {
            this.endGame();
            return;
        }

        this.currentGame = this.getMatchingMinigame(this.minigames[this.getCurrentGameId()]);
        if(this.currentGame == null) return;

        this.currentGame.startMinigame();
        this.currentGameID = this.getCurrentGameId()+1;
    }

    /**
     * Checks if the game contains a certain minigame type
     *
     * @param type   Type to check
     * @return       Whether or not the given type is in the minigame list
     */
    public boolean containsMinigame(GameType type) {
        boolean allowDuplicateGames = Parameters.ALLOW_DUPLICATE_GAMES();
        boolean hasEnoughGames = (GameType.values().length >= this.minigames.length);

        if(allowDuplicateGames || !hasEnoughGames) return false;

        for(GameType minigameType : this.minigames) {
            if(type == minigameType)
                return true;
        }
        return false;
    }

    /**
     * Returns a Minigame object of the given GameType
     *
     * @param type   Type of minigame to generate
     * @return       A {@link com.lielamar.partygames.game.Minigame} object
     */
    private Minigame getMatchingMinigame(GameType type) {
        if(type == GameType.ANIMAL_SLAUGHTER)
            return new AnimalSlaughter(this, type, "Animal Slaughter", 45, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.ANVIL_SPLEEF)
            return new AnvilSpleef(this, type, "Anvil Spleef", 120, Minigame.ScoreboardType.GAME_SCORE);
        if(type == GameType.AVALANCHE)
            return new Avalanche(this, type, "Avalanche", 5, Minigame.ScoreboardType.GAME_SCORE);
        if(type == GameType.BOMBARDMENT)
            return new Bombardment(this, type, "Bombardment", 180, Minigame.ScoreboardType.GAME_SCORE);
        if(type == GameType.CANNON_PAINTERS)
            return new CannonPainters(this, type, "Cannon Painters",30, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.CHICKEN_RINGS)
            return new ChickenRings(this, type, "Chicken Rings",240, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.DIVE)
            return new Dive(this, type, "Dive", 60, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.FIRE_LEAPERS)
            return new FireLeapers(this, type, "Fire Leapers", 150, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.FROZEN_FLOOR)
            return new FrozenFloor(this, type, "Frozen Floor", 120, Minigame.ScoreboardType.GAME_SCORE);
        if(type == GameType.HIGH_GROUND)
            return new HighGround(this, type, "High Ground", 75, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.HOE_HOE_HOE)
            return new HoeHoeHoe(this, type, "Hoe Hoe Hoe", 60, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.JIGSAW_RUSH)
            return new JigsawRush(this, type, "Jigsaw Rush", 120, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.JUNGLE_JUMP)
            return new JungleJump(this, type, "Jungle Jump", 120, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.LAB_ESCAPE)
            return new LabEscape(this, type, "Lab Escape", 180, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.LAWN_MOOWER)
            return new LawnMoower(this, type, "Lawn Moower", 60, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.MINECART_RACING)
            return new MinecartRacing(this, type, "Minecart Racing", 120, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.PIG_FISHING)
            return new PigFishing(this, type, "Pig Fishing", 90, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.PIG_JOUSTING)
            return new PigJousting(this, type, "Pig Jousting", 120, Minigame.ScoreboardType.GAME_SCORE);
        if(type == GameType.RPG_16)
            return new RPG16(this, type, "RPG-16", 120, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.SHOOTING_RANGE)
            return new ShootingRange(this, type, "Shooting Range", 60, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.SPIDER_MAZE)
            return new SpiderMaze(this, type, "Spider Maze", 120, Minigame.ScoreboardType.GAME_SCORE);
        if(type == GameType.SUPER_SHEEP)
            return new SuperSheep(this, type, "Super Sheep", 120, Minigame.ScoreboardType.GAME_SCORE);
        if(type == GameType.THE_FLOOR_IS_LAVA)
            return new TheFloorIsLava(this, type, "The Floor Is Lava", 120, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.TRAMPOLINIO)
            return new Trampolinio(this, type, "Trampolinio", 120, Minigame.ScoreboardType.MINIGAME_SCORE);
        if(type == GameType.VOLCANO)
            return new Volcano(this, type, "Volcano", 180, Minigame.ScoreboardType.GAME_SCORE);
        if(type == GameType.WORKSHOP)
            return new Workshop(this, type, "Workshop", 240, Minigame.ScoreboardType.MINIGAME_SCORE);

        return null;
    }

    /**
     * Ends the game
     */
    public void endGame() {
        CustomPlayer[] topPlayers = GameUtils.sortCustomPlayersList(this.getPlayers(), GameUtils.SortType.BY_SCORE, true);
        GameEndEvent event = new GameEndEvent(this, topPlayers[0], topPlayers[1], topPlayers[2], topPlayers);
        Bukkit.getPluginManager().callEvent(event);

        this.state = GameState.GAME_END;

        this.bossbarPlayers(null);

        for(CustomPlayer cp : this.getPlayers()) {
            if(cp == null) continue;
            this.fixPlayer(cp.getPlayer());

            this.scoreboardPlayers(ScoreboardUtils.assembleScoreboard(gamestatsscoreboard,
                    new Pair<>("%date%", TextUtils.getDate()),
                    new Pair<>("%game%", "end"),
                    new Pair<>("%gamefirst%", (topPlayers[0] != null) ? ChatColor.getLastColors(topPlayers[0].getPlayer().getDisplayName()) + topPlayers[0].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + topPlayers[0].getScore() + ChatColor.YELLOW + "✯" : ""),
                    new Pair<>("%gamesecond%", (topPlayers[1] != null) ? ChatColor.getLastColors(topPlayers[1].getPlayer().getDisplayName()) + topPlayers[1].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + topPlayers[1].getScore() + ChatColor.YELLOW + "✯" : ""),
                    new Pair<>("%gamethird%", (topPlayers[2] != null) ? ChatColor.getLastColors(topPlayers[2].getPlayer().getDisplayName()) + topPlayers[2].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + topPlayers[2].getScore() + ChatColor.YELLOW + "✯" : ""),
                    new Pair<>("%threedots%", (topPlayers[0] == cp || topPlayers[1] == cp || topPlayers[2] == cp) ? "" : "..."),
                    new Pair<>("%gameplayer%", (topPlayers[0] == cp || topPlayers[1] == cp || topPlayers[2] == cp) ? "" : ChatColor.getLastColors(cp.getPlayer().getDisplayName()) + cp.getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + cp.getScore() + ChatColor.YELLOW + "✯"),
                    new Pair<>("%currentgameid%", this.getCurrentGameId() + ""),
                    new Pair<>("%amountofgames%", Parameters.AMOUNT_OF_GAMES() + ""),
                    new Pair<>("%stars%", cp.getScore() + "")));
        }
    }


    /**
     * Fixes the player
     *
     * @param player   Player to fix
     */
    public void fixPlayer(Player player) {
        player.teleport(this.lobby);

        for(CustomPlayer cp : getPlayers()) {
            if(cp == null) continue;
            cp.getPlayer().showPlayer(player);
            player.showPlayer(cp.getPlayer());
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFoodLevel(20);
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setLevel(0);
        player.setExp(0);
        player.setFireTicks(0);

        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.getInventory().clear();
        player.updateInventory();

        for(PotionEffect pe : player.getActivePotionEffects())
            player.removePotionEffect(pe.getType());

        if(this.getGameState() == GameState.WAITING_FOR_PLAYERS) {
            this.scoreboardPlayers(ScoreboardUtils.assembleScoreboard(waitscoreboard, new Pair<>("%date%", TextUtils.getDate()),
                    new Pair<>("%map%", "Party Games"),
                    new Pair<>("%players%", this.getAmountOfPlayers() + ""),
                    new Pair<>("%maxplayers%", Parameters.MAXIMUM_PLAYERS() + ""),
                    new Pair<>("%remainingtime%", TextUtils.formatSeconds(this.getRemainingTime()) + "")));
        }
    }

    /**
     * Calculates the player index in the players array
     *
     * @param player   Player to check
     * @return         The index of the given player (-1 if they don't exist)
     */
    public int getPlayerIndex(Player player) {
        for(int i = 0; i < this.getPlayers().length; i++) {
            CustomPlayer p = this.getPlayers()[i];

            if(p == null) continue;

            if(p.getPlayer() == player)
                return i;
        }
        return -1;
    }

    /**
     * Checks if a player is in the game
     *
     * @param player   Player to check
     * @return         Whether or not player is in the game
     */
    public boolean containsPlayer(Player player) {
        for(int i = 0; i < this.getPlayers().length; i++) {
            CustomPlayer p = this.getPlayers()[i];

            if(p == null) continue;

            if(p.getPlayer() == player)
                return true;
        }
        return false;
    }

    /**
     * Calculates the amount of players in the game
     *
     * @return   Amount of players
     */
    public int getAmountOfPlayers() {
        int amount_of_players = 0;
        for(CustomPlayer p : this.getPlayers()) {
            if(p != null)
                amount_of_players++;
        }

        return amount_of_players;
    }


    public Main getMain() {
        return this.main;
    }
    public GameState getGameState() { return this.state; }

    public int getRemainingTime() { return this.remainingTime; }

    public CustomPlayer[] getPlayers() {
        return this.players;
    }
    public GameType[] getMinigames() { return this.minigames; }

    public Minigame getCurrentGame() {
        return this.currentGame;
    }
    public void setCurrentGameId(int id) { this.currentGameID = id; }
    public int getCurrentGameId() { return this.currentGameID; }

    public List<String> getGameStatsScoreboard() { return this.gamestatsscoreboard; }

    public Scoreboard getSpectatorsScoreboard() { return this.spectatorsScoreboard; }
    public Scoreboard getStaffScoreboard() { return this.staffScoreboard; }


    /**
     * Sends an info message to all players in the game
     *
     * @param msg   Message to send
     */
    public void infoPlayers(String msg) {
        for(CustomPlayer p : this.getPlayers()) {
            if(p != null) p.getPlayer().sendMessage(msg);
        }
    }

    /**
     * Sends a title to all players in the game
     *
     * @param msg   Title to send
     */
    public void titlePlayers(String msg) {
        for(CustomPlayer p : this.getPlayers()) {
            if(p != null) PacketManager.sendTitle(p.getPlayer(), msg, null,10, 20, 10);
        }
    }

    /**
     * Sends an action bar to all players in the game
     *
     * @param msg   Action Bar to send
     */
    public void actionbarPlayers(String msg) {
        for(CustomPlayer p : this.getPlayers()) {
            if(p != null) PacketManager.sendActionbar(p.getPlayer(), msg);
        }
    }

    /**
     * Sends a boss bar to all players in the game
     *
     * @param msg   BossBar to send
     */
    public void bossbarPlayers(String msg) {
        for(CustomPlayer p : this.getPlayers()) {
            if(p != null) {
                if(msg == null) p.destroyBossBar();
                else p.updateBossBar(msg);
            }
        }
    }

    /**
     * Sends a scoreboard to all players in the game
     *
     * @param lines   Lines assembling the scoreboard
     */
    public void scoreboardPlayers(ScoreboardManager.Line[] lines) {
        for(CustomPlayer cp : this.getPlayers()) {
            if(cp == null) continue;
            if(main.getScoreboardManager().getScoreboard(cp.getPlayer()) == null) continue;
            main.getScoreboardManager().getScoreboard(cp.getPlayer()).updateLines(lines);
        }
    }

    /**
     * Sends a boss bar to all players in the game
     *
     * @param sound    Sound to play
     * @param pitch    Pitch of the sound
     * @param volume   Volume of the sound
     */
    public void playSound(Sound sound, float pitch, float volume) {
        for(CustomPlayer p : this.getPlayers()) {
            if(p != null)
                p.getPlayer().playSound(p.getPlayer().getLocation(), sound, pitch, volume);
        }
    }
}