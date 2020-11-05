package com.lielamar.partygames.game;

import com.lielamar.lielsutils.SpigotUtils;
import com.lielamar.lielsutils.TextUtils;
import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.lielsutils.scoreboard.ScoreboardManager;
import com.lielamar.lielsutils.scoreboard.ScoreboardUtils;
import com.lielamar.lielsutils.validation.Validation;
import com.lielamar.partygames.events.MinigameEndEvent;
import com.lielamar.partygames.events.PlayerFinishMinigameEvent;
import com.lielamar.partygames.models.CustomPlayer;
import com.lielamar.partygames.models.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.utils.GameUtils;
import com.lielamar.partygames.utils.Parameters;
import com.packetmanager.lielamar.PacketManager;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Minigame {

    private Game game;
    private GameType gameType;
    private int gameTime;
    private String minigameName;

    private List<String> scoreboardLines;

    protected BukkitTask startMinigameTask;
    protected BukkitTask runMinigameTask;
    protected BukkitTask stopMinigameTask;

    protected GameState gameState;
    protected ScoreboardType scoreboardType;
    protected CustomPlayer[] finishedPlayers;

    protected Location middle;
    protected Location[] locations;

    private int elapsedTime;
    private int amountOfSpectators;

    public Minigame(Game game, GameType gameType, String minigameName, int gameTime, ScoreboardType scoreboardType) {
        this.game = game;
        this.gameType = gameType;
        this.gameTime = gameTime;
        this.minigameName = minigameName;

        this.scoreboardLines = this.game.getMain().getFileManager()
                .getConfig(Parameters.MINIGAMES_DIR() + minigameName).getConfig().getStringList("scoreboard");

        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.scoreboardType = scoreboardType;
        this.finishedPlayers = new CustomPlayer[3];

        this.setupMinigameParameters();
        this.setupMinigame();
        this.updateMinigameScoreboard();
    }

    /**
     * Sets-up all minigames parameters
     */
    public void setupMinigameParameters() {
        YamlConfiguration config = game.getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + getMinigameName()).getConfig();

        this.middle = SpigotUtils.fetchLocation(config, "middle");
        this.locations = SpigotUtils.fetchLocations(config, "locations");
        this.middle.getWorld().setTime(0);

        setElapsedTime(0);
        setAmountOfSpectators(0);
    }

    /**
     * Sets up the minigame
     */
    public void setupMinigame() {
        this.teleportPlayers();
    }

    /**
     * Teleports all online players to a location within the arena and then teleports all minigame players to their own location/minigame main location
     */
    public void teleportPlayers() {
        boolean perPlayerLocation = getLocations().length > 0;

        for(Player pl : Bukkit.getOnlinePlayers()) {
            if(pl == null) continue;
            pl.teleport((perPlayerLocation) ? getLocations()[0] : middle);
        }

        for(int i = 0; i < this.getGame().getPlayers().length; i++) {
            if(this.getGame().getPlayers()[i] == null) continue;

            // Teleports the player to either:
            // - A Provided location (if there are more players than locations, it teleports multiple players to the same location)
            // - The middle location
            this.initiatePlayer(this.getGame().getPlayers()[i],
                    (perPlayerLocation) ? this.getLocations()[i%this.getLocations().length] : this.getMiddle());

            PacketManager.sendTitle(this.getGame().getPlayers()[i].getPlayer(), "", "", 0, 10, 0);
        }
    }


    /**
     * Starts the game countdown timer
     */
    public void startMinigame() {
        if(stopMinigameTask != null) return;

        GameUtils.printMinigamePreparation(this);

        this.gameState = GameState.COUNTING_DOWN;
        this.startMinigameTask = new BukkitRunnable() {
            int i = Parameters.MINIGAME_START_TIME();

            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    runMinigame();
                    startMinigameTask = null;
                    return;
                }

                if(i <= 5)
                    game.playSound(Sound.WOOD_CLICK, 1F, 1F);

                game.bossbarPlayers(ChatColor.GOLD + "" + ChatColor.BOLD + "Game Start: " + ChatColor.RED + i);

                elapsedTime++;
                i--;
            }
        }.runTaskTimer(getGame().getMain(), 0L, 20L);
    }

    /**
     * Starts the minigame timer
     */
    public void runMinigame() {
        if(this.startMinigameTask != null) this.startMinigameTask.cancel();

        this.gameState = GameState.IN_GAME;
        this.extraStartParameters();

        this.runMinigameTask = new BukkitRunnable() {
            int i = gameTime;

            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    stopMinigame();
                    runMinigameTask = null;
                    return;
                }

                game.bossbarPlayers(ChatColor.GOLD + "" + ChatColor.BOLD + "Game End: " + ChatColor.RED + i);

                updateMinigameScoreboard();

                elapsedTime++;
                i--;
            }
        }.runTaskTimer(getGame().getMain(), 0L, 20L);
    }

    /**
     * Extra parameters for the game (runs when the game countdown ends/game starts)
     */
    public void extraStartParameters() {}

    /**
     * Starts the minigame end timer
     */
    public void stopMinigame() {
        CustomPlayer[] players = GameUtils.sortCustomPlayersList(this.getGame().getPlayers(), GameUtils.SortType.BY_MINIGAME_SCORE, true);
        MinigameEndEvent event = new MinigameEndEvent(this, players[0], players[1], players[2], players);
        Bukkit.getPluginManager().callEvent(event);

        if(this.startMinigameTask != null) this.startMinigameTask.cancel();
        if(this.runMinigameTask != null) this.runMinigameTask.cancel();

        this.scoreboardLines = this.getGame().getGameStatsScoreboard();
        this.scoreboardType = ScoreboardType.GAME_SCORE;

        this.gameState = GameState.GAME_END;
        stopMinigameTask = new BukkitRunnable() {
            int i = Parameters.MINIGAME_END_TIME();

            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    destroyMinigame();
                    stopMinigameTask = null;
                    return;
                }

                game.bossbarPlayers(ChatColor.GOLD + "" + ChatColor.BOLD + "Next Game: " + ChatColor.RED + i);
                updateMinigameScoreboard();

                elapsedTime++;
                i--;
            }
        }.runTaskTimer(getGame().getMain(), 0L, 20L);
    }

    /**
     * Destroys the current minigame & attempts to run the next one
     */
    public void destroyMinigame() {
        this.minigameName = null;
        this.gameState = null;
        this.locations = null;
        this.middle = null;
        this.game.runNextMinigame();
    }


    /**
     * Initiates the player. Set inventory, exp, gamemode, health, hunger and teleport to the location.
     * If we have a per player location for the current game we teleport each player to their location, else we teleport to middle.
     *
     * @param cp         {@link com.lielamar.partygames.models.CustomPlayer} Object to run the method on
     * @param location   Location to teleport the player to
     */
    public void initiatePlayer(CustomPlayer cp, Location location) {
        cp.setSpectator(false);
        cp.resetMinigameScore();

        Player player = cp.getPlayer();

        if(location != null)
            player.getPlayer().teleport(location);

        for(PotionEffect pe : player.getActivePotionEffects())
            player.removePotionEffect(pe.getType());

        for(CustomPlayer pl : this.getGame().getPlayers()) {
            if(pl == null) continue;
            player.showPlayer(pl.getPlayer());
            pl.getPlayer().showPlayer(player);
        }

        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.getInventory().clear();
        player.updateInventory();
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setExp(0);
        player.setLevel(0);
        player.setFoodLevel(20);
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFireTicks(0);
    }

    /**
     * Turns a player to a spectator
     *
     * @param cp                   {@link com.lielamar.partygames.models.CustomPlayer} Object to run the method on
     * @param isSpectatorState     Whether or not the player is an actual spectator or a finished player that turned to a spectator
     * @param canFly               Should allow flight for the provided player?
     * @param minigameScoreToAdd   Amount of score (minigame score) to add to the provided player
     */
    public void initiateSpectator(CustomPlayer cp, boolean isSpectatorState, boolean canFly, int minigameScoreToAdd) {
        for(PotionEffect pe : cp.getPlayer().getActivePotionEffects())
            cp.getPlayer().removePotionEffect(pe.getType());
        cp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1));

        cp.getPlayer().setAllowFlight(canFly);
        cp.getPlayer().setFlying(canFly);
        cp.getPlayer().getInventory().setHelmet(null);
        cp.getPlayer().getInventory().setChestplate(null);
        cp.getPlayer().getInventory().setLeggings(null);
        cp.getPlayer().getInventory().setBoots(null);
        cp.getPlayer().getInventory().clear();
        cp.getPlayer().updateInventory();
        cp.getPlayer().setExp(0);
        cp.getPlayer().setLevel(0);
        cp.getPlayer().setFoodLevel(20);
        cp.getPlayer().setHealth(20);

        cp.setSpectator(true);
        if(isSpectatorState) {
            PacketManager.sendTitle(cp.getPlayer(), ChatColor.RED + "You Died!", ChatColor.GRAY + "You are a spectator now", 5, 20, 5);
            amountOfSpectators++;
        }

        // Making spectators see each other, and players not see spectators
        for(CustomPlayer pl : getGame().getPlayers()) {
            if(pl == null) continue;

            if(!pl.isSpectator()) {
                pl.getPlayer().hidePlayer(cp.getPlayer());
                if(isSpectatorState)
                    pl.addMinigameScore(minigameScoreToAdd);
            } else {
                pl.getPlayer().showPlayer(cp.getPlayer());
                cp.getPlayer().showPlayer(pl.getPlayer());
            }
        }

        if(isSpectatorState) {
            if(getAmountOfSpectators() == this.getGame().getAmountOfPlayers() - 1)
                stopMinigame();
        }
    }

    /**
     * Adds a player to the finished players list and calculates whether or not the game needs to stop
     *
     * @param cp        {@link com.lielamar.partygames.models.CustomPlayer} to set as finished
     * @param message   Message to send the all players upon finishing
     */
    public void finishPlayer(CustomPlayer cp, String message) {
        if(cp == null || this.getFinishedPlayers()[0] == cp || this.getFinishedPlayers()[1] == cp || this.getFinishedPlayers()[2] == cp) return;

        this.getGame().infoPlayers(message);

        PlayerFinishMinigameEvent event = new PlayerFinishMinigameEvent(this, cp);

        if(this.getFinishedPlayers()[0] == null) {
            Bukkit.getPluginManager().callEvent(event);
            this.getFinishedPlayers()[0] = cp;
        } else if(this.getFinishedPlayers()[1] == null) {
            Bukkit.getPluginManager().callEvent(event);
            this.getFinishedPlayers()[1] = cp;

            if(this.getGame().getAmountOfPlayers()-amountOfSpectators == 2) {
                this.getFinishedPlayers()[0].setMinigameScore(3);
                cp.setMinigameScore(2);
                this.stopMinigame();
            }
        } else if(this.getFinishedPlayers()[2] == null) {

            this.getFinishedPlayers()[2] = cp;

            if(this.getGame().getAmountOfPlayers() > 3) {
                for(CustomPlayer player : this.getGame().getPlayers()) {
                    if(player == null) continue;
                    player.setMinigameScore(0);
                }
            }

            this.getFinishedPlayers()[0].setMinigameScore(3);
            this.getFinishedPlayers()[1].setMinigameScore(2);
            cp.setMinigameScore(1);

            this.stopMinigame();
        } else {
            return;
        }

        cp.getPlayer().getInventory().clear();
        cp.getPlayer().updateInventory();
    }


    /**
     * Updates the minigame scoreboard to all players individually
     *
     * @param additionalPairs   An array of {@link com.lielamar.lielsutils.modules.Pair} objects to add to the list of pairs we replace with the scoreboard
     */
    public void updateMinigameScoreboard(Pair<?, ?>... additionalPairs) {
        CustomPlayer[] sortedPlayers = getGame().getPlayers();
        if(scoreboardType == ScoreboardType.MINIGAME_SCORE)
            sortedPlayers = GameUtils.sortCustomPlayersList(getGame().getPlayers(), GameUtils.SortType.BY_MINIGAME_SCORE, false);
        else if(scoreboardType == ScoreboardType.GAME_SCORE)
            sortedPlayers = GameUtils.sortCustomPlayersList(getGame().getPlayers(), GameUtils.SortType.BY_SCORE, false);

        CustomPlayer cp;
        List<Pair<?, ?>> pairs;

        for(Player pl : Bukkit.getOnlinePlayers()) {
            ScoreboardManager.CustomScoreboard scoreboard = this.getGame().getMain().getScoreboardManager().getScoreboard(pl);
            if(scoreboard == null) continue;

            int playerIndex = this.getGame().getPlayerIndex(pl);
            if(playerIndex == -1) cp = null;
            else cp = this.getGame().getPlayers()[playerIndex];

            pairs = new ArrayList<>(Arrays.asList(additionalPairs));
            pairs.add(new Pair<>("%date%", TextUtils.getDate()));
            pairs.add(new Pair<>("%game%", getMinigameName()));
            pairs.add(new Pair<>("%minigamefirst%", (sortedPlayers[0] != null) ? ChatColor.getLastColors(sortedPlayers[0].getPlayer().getDisplayName()) + sortedPlayers[0].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + this.getMinigameScore(sortedPlayers[0]) : ""));
            pairs.add(new Pair<>("%gamefirst%", (sortedPlayers[0] != null) ? ChatColor.getLastColors(sortedPlayers[0].getPlayer().getDisplayName()) + sortedPlayers[0].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + sortedPlayers[0].getScore() + ChatColor.YELLOW + "✯" : ""));
            pairs.add(new Pair<>("%minigamesecond%", (sortedPlayers[1] != null) ? ChatColor.getLastColors(sortedPlayers[1].getPlayer().getDisplayName()) + sortedPlayers[1].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + this.getMinigameScore(sortedPlayers[1]) : ""));
            pairs.add(new Pair<>("%gamesecond%", (sortedPlayers[1] != null) ? ChatColor.getLastColors(sortedPlayers[1].getPlayer().getDisplayName()) + sortedPlayers[1].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + sortedPlayers[1].getScore() + ChatColor.YELLOW + "✯" : ""));
            pairs.add(new Pair<>("%minigamethird%", (sortedPlayers[2] != null) ? ChatColor.getLastColors(sortedPlayers[2].getPlayer().getDisplayName()) + sortedPlayers[2].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + this.getMinigameScore(sortedPlayers[2]) : ""));
            pairs.add(new Pair<>("%gamethird%", (sortedPlayers[2] != null) ? ChatColor.getLastColors(sortedPlayers[2].getPlayer().getDisplayName()) + sortedPlayers[2].getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + sortedPlayers[2].getScore() + ChatColor.YELLOW + "✯" : ""));
            pairs.add(new Pair<>("%threedots%", (cp == null || (sortedPlayers[0] == cp || sortedPlayers[1] == cp || sortedPlayers[2] == cp)) ? "" : "..."));
            pairs.add(new Pair<>("%minigameplayer%", (cp == null || (sortedPlayers[0] == cp || sortedPlayers[1] == cp || sortedPlayers[2] == cp)) ? "" : ChatColor.getLastColors(cp.getPlayer().getDisplayName()) + cp.getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + this.getMinigameScore(cp)));
            pairs.add(new Pair<>("%gameplayer%", (cp == null || (sortedPlayers[0] == cp || sortedPlayers[1] == cp || sortedPlayers[2] == cp)) ? "" : ChatColor.getLastColors(cp.getPlayer().getDisplayName()) + cp.getPlayer().getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + cp.getScore() + ChatColor.YELLOW + "✯"));
            pairs.add(new Pair<>("%playersleft%", (this.getGame().getAmountOfPlayers() - this.getAmountOfSpectators()) + ""));
            pairs.add(new Pair<>("%elapsedtime%", TextUtils.formatSeconds(this.getElapsedTime())));
            pairs.add(new Pair<>("%currentgameid%", this.getGame().getCurrentGameId() + ""));
            pairs.add(new Pair<>("%amountofgames%", Parameters.AMOUNT_OF_GAMES() + ""));
            pairs.add(new Pair<>("%stars%", (cp == null) ? "" : cp.getScore() + ""));

            scoreboard.updateLines(ScoreboardUtils.assembleScoreboard(getScoreboardLines(), pairs.toArray(new Pair[0])));
        }
    }

    /**
     * Calculates the minigame score of a player
     *
     * @param cp   Player to get the score of
     * @return     The minigame score of the given player
     */
    public int getMinigameScore(CustomPlayer cp) { return cp.getMinigameScore(); }

    /**
     * Checks all provided validations (parameters) and assembles a list of violations.
     *
     * @param validations                       An array of all validations to make
     * @throws MinigameConfigurationException   Throws an exception if there are violations
     */
    public void validateVariables(Validation...  validations) throws MinigameConfigurationException {
        List<Validation> violations = Validation.validateParameters(validations);

        if(violations.size() > 0) {
            stopMinigame();
            for(Validation violation : violations)
                throw new MinigameConfigurationException("MinigameConfigurationException: " + violation.getMessage());
        }
    }


    public Game getGame() { return this.game; }
    public GameType getGameType() { return this.gameType; }
    public int getGameTime() { return this.gameTime; }

    public String getMinigameName() { return this.minigameName; }
    public List<String> getScoreboardLines() { return this.scoreboardLines; }

    public GameState getGameState() { return this.gameState; }
    public ScoreboardType getScoreboardType() { return this.scoreboardType; }
    public CustomPlayer[] getFinishedPlayers() { return this.finishedPlayers; }

    public Location getMiddle() { return this.middle; }
    public Location[] getLocations() { return this.locations; }

    public int getElapsedTime() { return this.elapsedTime; }
    public void setElapsedTime(int time) {
        if(time < 0) time = 0;
        this.elapsedTime = time;
    }
    public int getAmountOfSpectators() { return this.amountOfSpectators; }
    public void setAmountOfSpectators(int amount) {
        if(amount < 0) amount = 0;
        this.amountOfSpectators = amount;
    }


    public enum ScoreboardType {
        MINIGAME_SCORE,
        GAME_SCORE
    }
}