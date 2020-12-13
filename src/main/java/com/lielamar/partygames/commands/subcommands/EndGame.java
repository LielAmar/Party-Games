package com.lielamar.partygames.commands.subcommands;

import com.lielamar.lielsutils.commands.Command;
import com.lielamar.partygames.PartyGames;
import com.lielamar.partygames.game.GameState;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndGame extends Command {

    private PartyGames main;

    public EndGame(PartyGames main, String name) {
        super(name);

        this.main = main;
    }

    @Override
    public String getDescription() {
        return "Ends a game";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "end" };
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "partygames.admin.endgame" };
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if(!hasPermissions(cs)) {
            cs.sendMessage(ChatColor.RED + "You don't have enough permissions to do that!");
            return;
        }

        if(main.getGame().getGameState() == GameState.GAME_END) {
            cs.sendMessage(ChatColor.RED + "This game has already ended!");
            return;
        }

        main.getGame().setCurrentGameId(main.getGame().getMinigames().length+1);
        main.getGame().getCurrentGame().stopMinigame();
        main.getGame().endGame();

        if(cs instanceof Player)
            main.getGame().infoPlayers(((Player)cs).getDisplayName() + ChatColor.YELLOW + " is ending the game...");
        else
            main.getGame().infoPlayers(ChatColor.GRAY + cs.getName() + ChatColor.YELLOW + " is ending the game...");
    }
}