package com.lielamar.partygames.modules.objects;

import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.modules.CustomPlayer;
import com.packetmanager.lielamar.PacketManager;
import com.packetmanager.lielamar.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Laser {

    private static final int TPS = 20;

    private Game game;
    private List<Location> startLocations;
    private double endLocation;

    private char axis;
    private int secondsToReachEnd;

    private double distance;
    private int amount_of_particles;

    public Laser(Game game, List<Location> startLocations, double endLocation, char axis, int secondsToReachEnd, double distance, int amount_of_particles) {
        this.game = game;
        this.startLocations = startLocations;
        this.endLocation = endLocation;

        this.axis = axis;
        this.secondsToReachEnd = secondsToReachEnd;

        this.distance = distance;
        this.amount_of_particles = amount_of_particles;

        this.run();
    }

    /**
     * Displays a laser going forward in the given direction in the constructor.
     */
    public void run() {
        double distanceHolder = matchSpeed();

        new BukkitRunnable() {

            int i = 0;

            @Override
            public void run() {
                if(i == secondsToReachEnd*20) {
                    for(CustomPlayer cp : game.getPlayers()) {
                        if(cp == null) continue;
                        if(cp.isSpectator()) continue;;
                        cp.getPlayer().playSound(cp.getPlayer().getLocation(), Sound.SUCCESSFUL_HIT, 1F, 1F);
                        cp.addMinigameScore(1);
                    }
                    this.cancel();
                    return;
                }

                Location modified = startLocations.get(0);

                for(int j = 0; j < game.getPlayers().length; j++) {
                    CustomPlayer cp = game.getPlayers()[j];
                    if(cp == null) continue;

                    for(Location loc : startLocations) {
                        if(axis == 'x')
                            modified = loc.clone().add(distanceHolder * i, 0, 0);
                        else if(axis == 'z')
                            modified = loc.clone().add(0, 0, distanceHolder * i);

                        PacketManager.sendParticle(cp.getPlayer(), ParticleEffect.CRIT, modified, 0, 0, 0, 0, amount_of_particles);

                        if(game.getCurrentGame() == null) continue;
                        double distance = Math.sqrt(Math.pow(cp.getPlayer().getLocation().getX()-modified.getX(), 2) + Math.pow(cp.getPlayer().getLocation().getZ()-modified.getZ(), 2));
                        if(!cp.isSpectator() && distance < 0.8 && Math.abs(cp.getPlayer().getLocation().getY()-modified.getY()) < 0.5)
                            game.getCurrentGame().initiateSpectator(cp, true, true, 1);
                    }
                }

                i++;
            }
        }.runTaskTimer(this.game.getMain(), 0L, 1L);
    }

    /**
     * @return   Speed as argument of Time.
     */
    public double matchSpeed() {
        double distance = this.distance;

        if(this.axis == 'x')
            distance = Math.abs(this.startLocations.get(0).getX()-endLocation);
        else if(this.axis == 'z')
            distance = Math.abs(this.startLocations.get(0).getZ()-endLocation);

        return (distance/this.secondsToReachEnd)/TPS;
    }
}