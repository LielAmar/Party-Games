package com.lielamar.partygames.models.entities;

import com.lielamar.partygames.models.entities.pathfindergoals.PathfinderGoalWrapper;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityVillager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WorkshopKeeper extends EntityVillager {

    private PathfinderGoalWrapper pathfinderGoalWrapper;
    private boolean isCustomEntity;

    public WorkshopKeeper(World world) {
        super(((CraftWorld)world).getHandle());
        this.isCustomEntity = true;

        this.pathfinderGoalWrapper = new PathfinderGoalWrapper(this);
        this.pathfinderGoalWrapper.setupPathfinderGoals(true, null, null);
    }

    /**
     * Entity move
     *
     * @param sideMot   Side Motion
     * @param forMot    Forward Motion
     */
    @Override
    public void g(float sideMot, float forMot) {
        if(isCustomEntity)
            return;

        super.g(sideMot, forMot);
    }

    @Override
    public void collide(Entity entity) {
        if(!isCustomEntity)
            super.collide(entity);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if(!isCustomEntity)
            return super.damageEntity(damagesource, f);
        return false;
    }


    /**
     * Spawns a workshop villager at location
     *
     * @param location   Location to teleport the workshop villager to
     * @return           Created workshop villager
     */
    public WorkshopKeeper spawnCustomEntity(Location location) {
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftLivingEntity) getBukkitEntity()).setRemoveWhenFarAway(false);

        ((CraftWorld)location.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return this;
    }
}