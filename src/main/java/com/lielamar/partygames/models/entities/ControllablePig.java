package com.lielamar.partygames.models.entities;

import com.packetmanager.lielamar.PacketManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class ControllablePig extends EntityPig {

    private Plugin plugin;
    private BukkitTask constantMovementTask;

    private boolean isCustom;
    private boolean canMove;
    private boolean constantMovement;
    private double speed;

    public ControllablePig(Plugin plugin, World world) {
        super(((CraftWorld)world).getHandle());

        this.plugin = plugin;
        this.constantMovementTask = null;

        this.isCustom = true;
        this.canMove = false;
        this.constantMovement = false;
        this.speed = 1.08;

        setupPathfinderGoals();
    }

    public ControllablePig(net.minecraft.server.v1_8_R3.World world) {
        super(world);

        this.isCustom = false;
    }

    /**
     * Entity move
     *
     * @param sideMot   Side Motion
     * @param forMot    Forward Motion
     */
    @Override
    public void g(float sideMot, float forMot) {
        if(!isCustom) {
            super.g(sideMot, forMot);
            return;
        }

        if(this.passenger == null || !(this.passenger instanceof EntityHuman)) return;
        if(!this.canMove)                                                      return;

        EntityHuman human = (EntityHuman) this.passenger;
        this.lastYaw = this.yaw = human.yaw;
        this.pitch = human.pitch * 0.5F;

        this.setYawPitch(this.yaw, this.pitch);

        if(Math.abs(motY) != 0.2) this.motY = 0; // Allow changes of Y level only when we send it through {@link ControllableEntitiesPacketReader [readChickenPacket] }
        super.g(sideMot, forMot);
    }

    @Override
    public void collide(Entity entity) {
        if(!isCustom)
            super.collide(entity);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if(!isCustom)
            return super.damageEntity(damagesource, f);
        return false;
    }


    /**
     * Destroys the custom entity (deletes it & everything related)
     */
    public void destroy() {
        getBukkitEntity().setPassenger(null);
        getBukkitEntity().remove();

        if(constantMovementTask != null)
            constantMovementTask.cancel();
    }

    /**
     * Sets up all PathfinderGoals of the entity
     */
    public void setupPathfinderGoals() {
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, targetSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, targetSelector)).clear();
    }

    /**
     * Spawns a controllable pig attached to player at location
     *
     * @param location   Location to teleport the pig to
     * @return           Created controllable pig
     */
    public ControllablePig spawnCustomEntity(Location location) {
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftLivingEntity) getBukkitEntity()).setRemoveWhenFarAway(false);

        ((CraftWorld)location.getWorld()).getHandle().addEntity(ControllablePig.this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return this;
    }

    /**
     * Attempts to start a timer that makes the entity constantly move (depending on {@param constantMovement}
     */
    public void updateConstantMovement() {
        if(constantMovementTask != null)
            constantMovementTask.cancel();

        if(constantMovement) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(!constantMovement) {
                        this.cancel();
                        return;
                    }
                    g(0, 0.98f);
                }
            }.runTaskTimer(plugin, 0, 1L);
        }
    }


    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
    public void setConstantMovement(boolean constantMovement) {
        this.constantMovement = constantMovement;
        if(constantMovement)
            updateConstantMovement();
    }

    public double getSpeed() { return this.speed; }
    public void setSpeed(double speed) {
        if(speed > 1.4)
            speed = 1.4;
        this.speed = speed;
    }
}