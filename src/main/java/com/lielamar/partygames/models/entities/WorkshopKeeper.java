package com.lielamar.partygames.models.entities;

import com.packetmanager.lielamar.PacketManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;

public class WorkshopKeeper extends EntityVillager {

    private boolean isCustom;

    public WorkshopKeeper(World world) {
        super(((CraftWorld)world).getHandle());

        this.isCustom = true;

        setupPathfinderGoals();
    }

    public WorkshopKeeper(net.minecraft.server.v1_8_R3.World world) {
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
        if(isCustom)
            return;

        super.g(sideMot, forMot);
    }

    /**
     * Entity drop item (egg)
     *
     * @param item     Item to drop
     * @param amount   Amount to drop
     * @return         Dropped Item
     */
    @Override
    public EntityItem a(Item item, int amount) {
        return null;
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

    @Override
    public void makeSound(String s, float f, float f1) {}


    /**
     * Sets up all PathfinderGoals to make it numb
     */
    public void setupPathfinderGoals() {
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, goalSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("b", PathfinderGoalSelector.class, targetSelector)).clear();
        ((List<?>) PacketManager.getPrivateField("c", PathfinderGoalSelector.class, targetSelector)).clear();
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