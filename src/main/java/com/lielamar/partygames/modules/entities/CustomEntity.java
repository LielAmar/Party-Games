package com.lielamar.partygames.modules.entities;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public interface CustomEntity {

    default EntityInsentient spawnCustomEntity(EntityInsentient entity, Location location) {
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftLivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);

        ((CraftWorld) location.getWorld()).getHandle().addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return entity;
    }

    /**
     * Destroys the custom entity
     */
    default void destroyCustomEntity(EntityInsentient entity) {
        entity.getBukkitEntity().setPassenger(null);
        entity.getBukkitEntity().remove();
    }
}
