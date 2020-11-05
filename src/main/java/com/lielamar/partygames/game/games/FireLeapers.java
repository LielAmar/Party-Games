package com.lielamar.partygames.game.games;

import com.lielamar.lielsutils.validation.CharValidation;
import com.lielamar.lielsutils.validation.DoubleValidation;
import com.lielamar.lielsutils.validation.IntValidation;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.objects.Laser;
import com.lielamar.partygames.utils.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class FireLeapers extends Minigame {

    private static char axis = 'x';
    private static double start_difficulty = 0;
    private static double default_distance = 20;
    private static int particles_per_square = 4;

    private double difficulty;
    private double minX, maxX, y, minZ, maxZ, distance;
    private List<Location> particleLocations;

    public FireLeapers(Game game, GameType gameType, String minigameName, int minigameTime, ScoreboardType scoreboardType) {
        super(game, gameType, minigameName, minigameTime, scoreboardType);
    }

    @Override
    public void setupMinigameParameters() {
        super.setupMinigameParameters();

        YamlConfiguration config = super.getGame().getMain().getFileManager().getConfig(Parameters.MINIGAMES_DIR() + super.getMinigameName()).getConfig();

        if(config.contains("parameters.axis")) axis = config.getString("parameters.axis").toLowerCase().charAt(0);
        if(config.contains("parameters.start_difficulty")) start_difficulty = config.getDouble("parameters.start_difficulty");
        if(config.contains("parameters.default_distance")) default_distance = config.getDouble("parameters.default_distance");
        if(config.contains("parameters.particles_per_square")) particles_per_square = config.getInt("parameters.particles_per_square");

        this.difficulty = start_difficulty;

        this.setupParticleLocations(config);

        try {
            super.validateVariables(
                    new CharValidation(axis, "[Fire Leapers] Axis must be from the allowed Axes list: x/z", new Character[] { 'x', 'z' }),
                    new DoubleValidation(start_difficulty, "[Fire Leapers] Start Difficulty must be greater than/equals to 0", 0),
                    new DoubleValidation(default_distance, "[Fire Leapers] Default Distance must be greater than 0", 1),
                    new IntValidation(particles_per_square, "[Fire Leapers] Particles Per Square must be greater than 0", 1));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupMinigame() {
        super.setupMinigame();

        for(int i = 0; i < super.getGame().getPlayers().length; i++) {
            if(super.getGame().getPlayers()[i] == null) continue;
            super.getGame().getPlayers()[i].getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999, 4));
        }
    }

    @Override
    public void runMinigame() {
        if(super.startMinigameTask != null) super.startMinigameTask.cancel();

        super.gameState = GameState.IN_GAME;
        super.extraStartParameters();

        super.runMinigameTask = new BukkitRunnable() {
            int i = getGameTime();

            @Override
            public void run() {
                if(i == 0) {
                    this.cancel();
                    stopMinigame();
                    runMinigameTask = null;
                    return;
                }

                if(i%9 == 0 || i < 70 && i%5 == 0 || i < 40 && i%3 == 0) {
                    new Laser(getGame(), particleLocations, maxX, axis, (difficulty < 6) ? 7 - (int)difficulty : 1, default_distance, particles_per_square);
                    difficulty+=0.25;
                }

                getGame().bossbarPlayers(ChatColor.GOLD + "" + ChatColor.BOLD + "Game End: " + ChatColor.RED + i);

                updateMinigameScoreboard();
                setElapsedTime(getElapsedTime()+1);

                i--;
            }
        }.runTaskTimer(getGame().getMain(), 0L, 20L);
    }


    /**
     * Sets up all locations where at particles are spawning
     *
     * @param config   Config to load data from
     */
    public void setupParticleLocations(YamlConfiguration config) {
        this.particleLocations = new ArrayList<>();

        this.minX = config.getInt("particles.minX");
        this.maxX = config.getInt("particles.maxX");
        this.y = config.getInt("particles.Y");
        this.minZ = config.getInt("particles.minZ");
        this.maxZ = config.getInt("particles.maxZ");
        this.distance = config.getDouble("particles.distance");
        World world = super.getMiddle().getWorld();

        double xDistance = this.distance;
        double zDistance = this.distance;
        if(this.minX > this.maxX) xDistance = this.distance*(-1.0);
        if(this.minZ > this.maxZ) zDistance = this.distance*(-1.0);

        if(axis == 'x') {
            for(double z = this.minZ; Math.abs(z) <= Math.abs(this.maxZ); z += zDistance)
                this.particleLocations.add(new Location(world, this.minX, this.y, z));
        } else {
            for(double x = this.minX; Math.abs(x) <= Math.abs(this.maxX); x+=xDistance)
                this.particleLocations.add(new Location(world, x, this.y, this.minZ));
        }
    }
}