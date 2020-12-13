package com.lielamar.partygames.events;

import com.lielamar.partygames.game.Game;
import org.bukkit.event.Event;

public abstract class PartyGamesEvent extends Event {

    protected Game game;

    public PartyGamesEvent(Game game) {
        this.game = game;
    }

    PartyGamesEvent(Game game, boolean async) {
        super(async);
        this.game = game;
    }

    public final Game getGame() {
        return this.game;
    }
}
