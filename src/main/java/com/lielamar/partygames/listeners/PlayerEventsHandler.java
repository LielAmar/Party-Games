package com.lielamar.partygames.listeners;

import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;

public class PlayerEventsHandler<T extends PlayerEvent & Cancellable> implements Listener {

    private String[] permissions;

    public PlayerEventsHandler(String... permissions) {
        this.permissions = permissions;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onEvent(T event) {
        event.setCancelled(true);

        if(permissions != null) {
            for(String permission : permissions) {
                if(event.getPlayer().hasPermission(permission))
                    event.setCancelled(false);
            }
        }
    }
}