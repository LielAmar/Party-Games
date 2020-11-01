package com.lielamar.partygames.models.entities;

import com.lielamar.partygames.models.entities.pathfindergoals.PathfinderGoalMeleeAttackPlayer;
import com.lielamar.partygames.models.entities.pathfindergoals.PathfinderGoalTargetPlayer;
import com.packetmanager.lielamar.PacketManager;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntitySpider;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;

public class ChasingSpider extends EntitySpider {

    private Player target;

    public ChasingSpider(World world, Player target) {
        super(((CraftWorld)world).getHandle());

        this.target = target;

        setupPathfinderGoals();
    }

    public ChasingSpider(net.minecraft.server.v1_8_R3.World world) {
        super(world);
    }

    /**
     * Removes all default PathfinderGoals and replaces them with custom ones
     */
    public void setupPathfinderGoals() {
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, targetSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, targetSelector)).clear();

        this.setGoalTarget(((CraftPlayer)this.target).getHandle(), EntityTargetEvent.TargetReason.CUSTOM, true);

        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttackPlayer(this, EntityHuman.class));     // Attack Pathfinder goal
        this.goalSelector.a(2, new PathfinderGoalTargetPlayer(this, 5,  10));           // Follow pathfinder goal
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