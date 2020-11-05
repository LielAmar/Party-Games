package com.lielamar.partygames.models.entities;

import com.lielamar.lielsutils.modules.Pair;
import com.lielamar.partygames.models.entities.pathfindergoals.PathfinderGoalMeleeAttackPlayer;
import com.lielamar.partygames.models.entities.pathfindergoals.PathfinderGoalTargetPlayer;
import com.lielamar.partygames.models.entities.pathfindergoals.PathfinderGoalWrapper;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntitySpider;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.List;

public class ChasingSpider extends EntitySpider {

    private Player target;
    private PathfinderGoalWrapper pathfinderGoalWrapper;

    public ChasingSpider(World world, Player target) {
        super(((CraftWorld)world).getHandle());

        this.target = target;

        this.pathfinderGoalWrapper = new PathfinderGoalWrapper(this);
        List<Pair<PathfinderGoal, Integer>> pathfinderGoals = new ArrayList<>();
        pathfinderGoals.add(new Pair<>(new PathfinderGoalFloat(this), 1));
        pathfinderGoals.add(new Pair<>(new PathfinderGoalMeleeAttackPlayer(this, EntityHuman.class), 2));
        pathfinderGoals.add(new Pair<>(new PathfinderGoalTargetPlayer(this, 5,  10), 2));
        this.pathfinderGoalWrapper.setupPathfinderGoals(true, ((CraftPlayer)this.target).getHandle(), pathfinderGoals);
    }

    /**
     * Spawns a chasing spider attached to player at location
     *
     * @param location   Location to teleport the spider to
     * @return           Created chasing spider
     */
    public ChasingSpider spawnCustomEntity(Location location) {
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftLivingEntity) getBukkitEntity()).setRemoveWhenFarAway(false);

        ((CraftWorld)location.getWorld()).getHandle().addEntity(ChasingSpider.this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return this;
    }
}