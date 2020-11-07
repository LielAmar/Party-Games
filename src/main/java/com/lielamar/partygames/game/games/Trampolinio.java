package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.*;
import com.lielamar.partygames.modules.CustomPlayer;
import com.lielamar.partygames.modules.exceptions.MinigameConfigurationException;
import com.lielamar.partygames.modules.objects.TrampolinioScore;
import com.lielamar.partygames.game.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Trampolinio extends Minigame implements Listener {

    private static int radius = 12;
    private static int amount_of_boosts = 2;
    private static int amount_of_points = 12;
    private static int one_score_percentage = 45;
    private static int three_score_percentage = 45;
    private static int ten_score_percentage = 10;
    private static List<Material> block_types = new ArrayList<>();

    private List<TrampolinioScore> scores;
    private List<TrampolinioScore> boosts;

    public Trampolinio(Game game, GameType gameType) {
        super(game, gameType);
        Bukkit.getPluginManager().registerEvents(this, this.getGame().getMain());
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        if(config.contains("parameters.radius")) radius = config.getInt("parameters.radius");
        if(config.contains("parameters.amount_of_boosts")) amount_of_boosts = config.getInt("parameters.amount_of_boosts");
        if(config.contains("parameters.amount_of_points")) amount_of_points = config.getInt("parameters.amount_of_points");
        if(config.contains("parameters.one_score_percentage")) one_score_percentage = config.getInt("parameters.one_score_percentage");
        if(config.contains("parameters.three_score_percentage")) three_score_percentage = config.getInt("parameters.three_score_percentage");
        if(config.contains("parameters.ten_score_percentage")) ten_score_percentage = config.getInt("parameters.ten_score_percentage");
        if(config.contains("parameters.block_types") && config.isList("parameters.block_types")) {
            for(String s : config.getStringList("parameters.block_types"))
                block_types.add(Material.valueOf(s));
        } else {
            block_types.add(Material.STAINED_GLASS);
            block_types.add(Material.WOOL);
        }

        this.scores = new ArrayList<>();
        this.boosts = new ArrayList<>();

        try {
            super.validateVariables(
                    new IntValidation(radius, "[Trampolinio] Radius must be greater than 0", 1),
                    new IntValidation(amount_of_boosts, "[Trampolinio] Amount of Boosts must be greater than 1", 2),
                    new IntValidation(amount_of_points, "[Trampolinio] Amount of Points must be greater than 0", 1),
                    new IntValidation(one_score_percentage, "[Trampolinio] One Score Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100),
                    new IntValidation(three_score_percentage, "[Trampolinio] Three Score Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100),
                    new IntValidation(ten_score_percentage, "[Trampolinio] Ten Score Percentage must be greater than/equals to 0 and less than/equals to 100", 1, 100));
        } catch(MinigameConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extraStartParameters() {
        super.extraStartParameters();

        for(CustomPlayer cp : super.getGame().getPlayers()) {
            if(cp == null) continue;
            cp.getPlayer().setVelocity(cp.getPlayer().getLocation().getDirection().multiply(1.35).setY(1.35));
            this.spawnScores();
            this.spawnBoosts();
        }
    }

    @Override
    public void destroyMinigame() {
        super.destroyMinigame();

        for(TrampolinioScore score : scores) score.destroy();
        for(TrampolinioScore score : boosts) score.destroy();

        scores = null;
        boosts = null;
    }


    /**
     * Spawns an X amount of scores whereas X = AMOUNT_OF_SCORES-CURRENT_AMOUNT_OF_SCORES+random
     */
    public void spawnScores() {
        int randomValue = Main.rnd.nextInt(5)-2;

        if(scores.size() >= amount_of_points + randomValue)
            return;

        // Spawns the scores
        while(scores.size() < amount_of_points + randomValue)
            scores.add(new TrampolinioScore(getRandomLocation(), getRandomItem()));
    }

    /**
     * Spawns an X amount of boosts whereas X = AMOUNT_OF_BOOSTS-CURRENT_AMOUNT_OF_BOOSTS+random
     */
    public void spawnBoosts() {
        int randomValue = Main.rnd.nextInt(2)-1;

        if(boosts.size() >= amount_of_boosts + randomValue)
            return;

        while(boosts.size() < amount_of_boosts + randomValue)
            boosts.add(new TrampolinioScore(getRandomLocation(), new ItemStack(Material.WEB)));
    }

    /**
     * Generates a random location withing RADIUS blocks from the middle location, as well as ~10 high
     *
     * @return   Generated location
     */
    public Location getRandomLocation() {
        return super.getMiddle().clone().add(Main.rnd.nextInt(radius*2+1)-radius,
                Main.rnd.nextInt(3)+6,Main.rnd.nextInt(radius*2+1)-radius);
    }

    /**
     * Returns a random item (score)
     *
     * @return   Random ItemStack of the score
     */
    public ItemStack getRandomItem() {
        int percentage = Main.rnd.nextInt(one_score_percentage + three_score_percentage + ten_score_percentage);

        if(percentage < one_score_percentage)
            return new ItemStack(Material.WOOL, 1, (byte)5);
        else if(percentage < one_score_percentage + three_score_percentage)
            return new ItemStack(Material.WOOL, 1, (byte)4);
        else
            return new ItemStack(Material.WOOL, 1, (byte)14);
    }

    /**
     * Checks if a player went though a score object
     *
     * @param cp   {@link CustomPlayer} to check
     */
    public void checkScores(CustomPlayer cp) {
        boolean changed = false;

        Iterator<TrampolinioScore> it = this.scores.iterator();
        while(it.hasNext()) {
            TrampolinioScore score = it.next();

            if(score.run(cp)) {
                it.remove();
                changed = true;

                if(cp.getMinigameScore() >= 40) {
                    stopMinigame();
                    return;
                }
            }
        }

        if(changed)
            spawnScores();
    }

    /**
     * Checks if a player went though a boost object
     *
     * @param cp   {@link CustomPlayer} to check
     */
    public void checkBoosts(CustomPlayer cp) {
        boolean changed = false;

        Iterator<TrampolinioScore> it = this.boosts.iterator();
        while(it.hasNext()) {
            TrampolinioScore score = it.next();

            if(score.run(cp)) {
                it.remove();
                changed = true;
            }
        }

        if(changed)
            spawnBoosts();
    }

    
    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerMove(PlayerMoveEvent e) {
        if(super.getGame() == null) return;
        if(super.getGame().getCurrentGame() == null) return;
        if(!(super.getGame().getCurrentGame() instanceof Trampolinio)) return;

        if(super.getGameState() != GameState.IN_GAME) return;

        int playerIndex = super.getGame().getPlayerIndex(e.getPlayer());
        if(playerIndex == -1) return;

        if(block_types.contains(e.getTo().getBlock().getRelative(BlockFace.DOWN).getType())) {
            if(e.getPlayer().isOnGround()) {
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.5).setY(1.5));
                return;
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(e.getPlayer().isOnGround())
                            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.35).setY(1.35));
                    }
                }.runTaskLater(super.getGame().getMain(), 20L);
            }
        }

        checkScores(super.getGame().getPlayers()[playerIndex]);
        checkBoosts(super.getGame().getPlayers()[playerIndex]);
    }
}