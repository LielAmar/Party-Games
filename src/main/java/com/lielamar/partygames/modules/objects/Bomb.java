package com.lielamar.partygames.modules.objects;

import com.lielamar.packetmanager.PacketManager;
import com.lielamar.packetmanager.ParticleEffect;
import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.modules.CustomPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Bomb {

    private static final int PHASE_1_SPEED = 5;
    private static final int PHASE_2_SPEED = 5;
    private static final int PHASE_3_SPEED = 3;
    private static final int PHASE_4_SPEED = 3;
    private static final int PHASE_5_SPEED = 2;

    private Game game;
    private ArmorStand bomb;

    private double xDiff, yDiff, zDiff;
    private int speed;

    public Bomb(Game game, Location from, Location to, Material material, int phase) {
        this.game = game;
        this.bomb = (ArmorStand) from.getWorld().spawnEntity(from, EntityType.ARMOR_STAND);
        this.bomb.setVisible(false);
        this.bomb.setGravity(false);
        this.bomb.setHelmet(new ItemStack(material));

        if(phase == 1) this.speed = PHASE_1_SPEED;
        else if(phase == 2) this.speed = PHASE_2_SPEED;
        else if(phase == 3) this.speed = PHASE_3_SPEED;
        else if(phase == 4) this.speed = PHASE_4_SPEED;
        else this.speed = PHASE_5_SPEED;

        this.xDiff = (to.getX()-from.getX())/(speed*20);
        this.yDiff = (to.getY()-from.getY())/(speed*20);
        this.zDiff = (to.getZ()-from.getZ())/(speed*20);

        run();
    }

    /**
     * Explodes the bomb in the given direction in the constructor.
     */
    public void run() {
        this.bomb.getWorld().createExplosion(this.bomb.getLocation().getX(), this.bomb.getLocation().getY(), this.bomb.getLocation().getZ(), 1.5F, false, false);
        game.playSound(Sound.EXPLODE, 0.5F, 1F);

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                bomb.teleport(bomb.getLocation().clone().add(xDiff, yDiff, zDiff));

                Location particleLoc = bomb.getLocation().clone().add(0, 1.5, 0);
                particleLoc.add(-4*xDiff, -4*yDiff, -4*zDiff);
                for(CustomPlayer cp : game.getPlayers()) {
                    if(cp == null) continue;
                    PacketManager.sendParticle(cp.getPlayer(), ParticleEffect.SPELL_MOB, particleLoc, 0, 0, 0, 0, 1);
                    PacketManager.sendParticle(cp.getPlayer(), ParticleEffect.FLAME, particleLoc, 0, 0, 0, 0, 1);

                    // If cp is 0.5 blocks away from the block - turn them to a spectator
                    if(game.getCurrentGame().getGameState() == GameState.IN_GAME) {
                        if(cp.getPlayer().getLocation().distance(bomb.getLocation()) < 1) {
                            if(game.getPlayerIndex(cp.getPlayer()) != -1)
                                game.getCurrentGame().initiateSpectator(cp, true, true, 1);
                        }
                    }
                }

                if(i == speed*20 || game.getCurrentGame().getGameState() != GameState.IN_GAME) {
                    this.cancel();
                    bomb.remove();
                    bomb.getWorld().createExplosion(bomb.getLocation().getX(), bomb.getLocation().getY()+3, bomb.getLocation().getZ(), 1.5F, false, false);
                }
                i++;
            }
        }.runTaskTimer(game.getMain(), 0L, 1L);
    }
}