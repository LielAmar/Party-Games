package com.lielamar.partygames.listeners;

import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;

public class ListenerHandler<T extends PlayerEvent & Cancellable> {

    private PlayerEvent event;

    public ListenerHandler(T event) {
        this.event = event;
    }

    private void executeEvent(String permission) {
        if(!event.getPlayer().hasPermission(permission))
            ((Cancellable)event).setCancelled(true);
    }
}