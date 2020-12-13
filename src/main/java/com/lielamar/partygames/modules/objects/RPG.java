package com.lielamar.partygames.modules.objects;

import com.lielamar.partygames.PartyGames;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.modules.CustomPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;

public class RPG {

    private Game game;
    private Player shooter;
    private CustomPlayer cpShooter;
    private Entity rpg;

    public RPG(Game game, Player shooter, Location from, EntityType rpgType) {
        this.game = game;
        this.shooter = shooter;
        int playerIndex = game.getPlayerIndex(shooter);
        if(playerIndex != -1)
            this.cpShooter = game.getPlayers()[playerIndex];

        this.rpg = from.getWorld().spawnEntity(from.add(0, 1, 0), rpgType);

        run();
    }

    /**
     * Lets the player control the rpg through their head movement
     */
    public void run() {
        new BukkitRunnable() {
            int i = 5*5;
            Location to;

            @Override
            public void run() {
                List<Entity> nearbyEntities = rpg.getNearbyEntities(1, 1, 1);
                if(i <= 0 || shooter == null || rpg.isOnGround() || rpg.getLocation().getWorld().getBlockAt(rpg.getLocation()) != null
                    && rpg.getLocation().getWorld().getBlockAt(rpg.getLocation()).getType().isSolid()
                    || rpg.getLocation().clone().add(1, 0, 0).getBlock().getType().isSolid()
                    || rpg.getLocation().clone().add(-1, 0, 0).getBlock().getType().isSolid()
                    || rpg.getLocation().clone().add(0, 1, 0).getBlock().getType().isSolid()
                    || rpg.getLocation().clone().add(0, 2, 0).getBlock().getType().isSolid()
                    || rpg.getLocation().clone().add(0, 0, 1).getBlock().getType().isSolid()
                    || rpg.getLocation().clone().add(0, 0, -1).getBlock().getType().isSolid()
                    || nearbyEntities.size() != 0 && !nearbyEntities.contains(shooter)
                    || nearbyEntities.size() > 1) {
                    explode();
                    this.cancel();

                    if(nearbyEntities.size() > 1)
                        shooter.playSound(shooter.getLocation(), Sound.HURT_FLESH, 1F, 1F);
                    return;
                }

                to = shooter.getTargetBlock((Set<Material>) null, 200).getLocation();
                rpg.setVelocity(to.toVector().subtract(rpg.getLocation().toVector()).normalize());

                i--;
            }
        }.runTaskTimer(game.getMain(), 0L, 4L);
    }

    /**
     * Explodes the rpg
     */
    private void explode() {
        Location loc = this.rpg.getLocation();
        this.rpg.remove();

        loc.getWorld().createExplosion(loc, 2F);

        for(Entity ent : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
            if(!(ent instanceof Player)) continue;
            if(ent == this.shooter) continue;

            Player p = (Player)ent;
            if(game.getPlayerIndex(p) == -1) continue;

            if(p.getHealth() - 2 <= 0) {
                p.playSound(ent.getLocation(), Sound.HURT_FLESH, 1F, 1F);
                p.teleport(this.game.getCurrentGame().getLocations()[PartyGames.rnd.nextInt(this.game.getCurrentGame().getLocations().length)]);
                p.setHealth(6);

                if(this.cpShooter.getPlayer() != null) this.cpShooter.addMinigameScore(1);
                game.infoPlayers(ChatColor.RED + p.getName() + ChatColor.YELLOW + " was killed by " + ChatColor.GREEN + shooter.getName());
            } else
                ((Player) ent).damage(2, this.shooter);
        }
    }


    public Entity getEntity() {
        return this.rpg;
    }
}