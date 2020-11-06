package com.lielamar.partygames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;

public class EntityEventsHandler<T extends EntityEvent & Cancellable> implements Listener {

    private String[] permissions;

    public EntityEventsHandler(String... permissions) {
        this.permissions = permissions;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEvent(T event) {
        if(event.getEntity() instanceof Player) {
            event.setCancelled(true);

            if(permissions != null) {
                for(String permission : permissions) {
                    if(event.getEntity().hasPermission(permission))
                        event.setCancelled(false);
                }
            }
        }
    }
}