package com.lielamar.partygames.events;

import com.lielamar.partygames.game.Game;
import com.lielamar.partygames.modules.CustomPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class GameEndEvent extends PartyGamesEvent implements Cancellable {

    private CustomPlayer first, second, third;
    private CustomPlayer[] players;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    public GameEndEvent(Game game, CustomPlayer first, CustomPlayer second, CustomPlayer third, CustomPlayer[] players){
        super(game);
        this.first = first;
        this.second = second;
        this.third = third;
        this.players = players;
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

    public CustomPlayer getFirst() {
        return this.first;
    }

    public CustomPlayer getSecond() {
        return this.second;
    }

    public CustomPlayer getThird() {
        return this.third;
    }

    public CustomPlayer[] getPlayers() {
        return this.players;
    }
}
