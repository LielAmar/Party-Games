package com.lielamar.partygames.models.entities;

import com.lielamar.lielsutils.MathUtils;
import com.packetmanager.lielamar.PacketManager;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class ShootingRangeZombie extends EntityZombie {

    private Location current, point1, point2;
    private int xDiff, yDiff, zDiff;
    private double speed;

    public ShootingRangeZombie(World world, Location point1, Location point2, double speed) {
        super(((CraftWorld)world).getHandle());

        this.point1 = current = point1;
        this.point2 = point2;

        this.speed = speed;
        this.xDiff = ((int)(current.getX()-this.getBukkitEntity().getLocation().getX()))/(int)current.distance(this.getBukkitEntity().getLocation());
        this.yDiff = ((int)(current.getY()-this.getBukkitEntity().getLocation().getY()))/(int)current.distance(this.getBukkitEntity().getLocation());
        this.zDiff = ((int)(current.getZ()-this.getBukkitEntity().getLocation().getZ()))/(int)current.distance(this.getBukkitEntity().getLocation());

        setupPathfinderGoals();
    }

    @Override
    public void g(float sideMot, float forMot) {
        super.g(0, forMot);

        // If the distance between the entity and the current (targeted) point is <= 1 (meaning they arrived at it)
        // we want to swap the current location to the other one, reset the <coordinate>Diff and relocate the entity
        if(MathUtils.XZDistance(this.getBukkitEntity().getLocation().getX(), this.current.getX(),
                this.getBukkitEntity().getLocation().getZ(), this.current.getZ()) <= 1) {
            if(current == point1) current = point2;
            else current = point1;

            // Re-setting the entity's location (specifically it's yaw) to avoid the entity turning around and getting out of the platform
            this.setLocation(getBukkitEntity().getLocation().getX(), getBukkitEntity().getLocation().getY(), getBukkitEntity().getLocation().getZ(),
                    MathUtils.getAngle(new Vector(getBukkitEntity().getLocation().getX(), 0, getBukkitEntity().getLocation().getZ()), current.toVector()), 0F);

            this.xDiff = ((int)(current.getX()-this.getBukkitEntity().getLocation().getX()))/(int)current.distance(this.getBukkitEntity().getLocation());
            this.yDiff = ((int)(current.getY()-this.getBukkitEntity().getLocation().getY()))/(int)current.distance(this.getBukkitEntity().getLocation());
            this.zDiff = ((int)(current.getZ()-this.getBukkitEntity().getLocation().getZ()))/(int)current.distance(this.getBukkitEntity().getLocation());

        }

        this.ticksFarFromPlayer = 0;

        // Move the entity
        this.getNavigation().a(getBukkitEntity().getLocation().getX()+xDiff, getBukkitEntity().getLocation().getY()+yDiff, getBukkitEntity().getLocation().getZ()+zDiff, this.speed);
    }

    /**
     * Removes collision
     *
     * @param entity   Collided entity
     */
    @Override
    public void collide(Entity entity) {}


    /**
     * Sets up all PathfinderGoals of the entity
     */
    public void setupPathfinderGoals() {
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, targetSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, targetSelector)).clear();

        this.goalSelector.a(0, new PathfinderGoalFloat(this));
    }

    /**
     * Spawns a Shooting Range zombie
     *
     * @return   Created Shooting Range zombie
     */
    public ShootingRangeZombie spawnCustomEntity() {
        setLocation(current.getX(), current.getY(), current.getZ(), current.getYaw(), current.getPitch());
        ((CraftLivingEntity) getBukkitEntity()).setRemoveWhenFarAway(true);

        ((CraftWorld)current.getWorld()).getHandle().addEntity(ShootingRangeZombie.this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return this;
    }
}