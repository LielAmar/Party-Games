package com.lielamar.partygames.events;

import com.lielamar.partygames.game.Minigame;
import com.lielamar.partygames.modules.CustomPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerFinishMinigameEvent extends Event {

    private Minigame minigame;
    private CustomPlayer player;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    public PlayerFinishMinigameEvent(Minigame minigame, CustomPlayer player) {
        this.minigame = minigame;
        this.player = player;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public void setCancelled(boolean choice) {
        this.cancelled = choice;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public Minigame getMinigame() {
        return this.minigame;
    }

    public CustomPlayer getPlayer() {
        return this.player;
    }
}
