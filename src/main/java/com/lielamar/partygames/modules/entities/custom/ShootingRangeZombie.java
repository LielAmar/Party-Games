package com.lielamar.partygames.modules.entities.custom;

import com.lielamar.lielsutils.MathUtils;
import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.partygames.modules.entities.CustomEntity;
import com.lielamar.partygames.modules.entities.pathfindergoals.PathfinderGoalWrapper;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ShootingRangeZombie extends EntityZombie implements CustomEntity {

    private PathfinderGoalWrapper pathfinderGoalWrapper;

    private Location current, point1, point2;
    private int xDiff, yDiff, zDiff;
    private double speed;

    public ShootingRangeZombie(World world, Location point1, Location point2, double speed) {
        super(((CraftWorld)world).getHandle());

        this.pathfinderGoalWrapper = new PathfinderGoalWrapper(this);
        List<Pair<PathfinderGoal, Integer>> pathfinderGoals = new ArrayList<>();
        pathfinderGoals.add(new Pair<>(new PathfinderGoalFloat(this), 1));
        this.pathfinderGoalWrapper.setupPathfinderGoals(true, null, pathfinderGoals);

        this.point1 = current = point1;
        this.point2 = point2;

        this.speed = speed;
        this.xDiff = ((int)(current.getX()-this.getBukkitEntity().getLocation().getX()))/(int)current.distance(this.getBukkitEntity().getLocation());
        this.yDiff = ((int)(current.getY()-this.getBukkitEntity().getLocation().getY()))/(int)current.distance(this.getBukkitEntity().getLocation());
        this.zDiff = ((int)(current.getZ()-this.getBukkitEntity().getLocation().getZ()))/(int)current.distance(this.getBukkitEntity().getLocation());
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

    @Override
    public void collide(Entity entity) {}
}