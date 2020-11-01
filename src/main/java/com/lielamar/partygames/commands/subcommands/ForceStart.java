package com.lielamar.partygames.commands.subcommands;

import com.lielamar.lielsutils.commands.Command;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.GameState;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceStart extends Command {

    private Main main;

    public ForceStart(Main main, String name) {
        super(name);

        this.main = main;
    }

    @Override
    public String getDescription() {
        return "Force Start a game";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "force", "start" };
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "partygames.admin.forcestart" };
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if(!hasPermissions(cs)) {
            cs.sendMessage(ChatColor.RED + "You don't have enough permissions to do that!");
            return;
        }

        if(main.getGame().getGameState() != GameState.WAITING_FOR_PLAYERS
            && main.getGame().getGameState() != GameState.COUNTING_DOWN) {
            cs.sendMessage(ChatColor.RED + "This game has already started!");
            return;
        }

        main.getGame().startGame();
        if(cs instanceof Player)
            main.getGame().infoPlayers(((Player)cs).getDisplayName() + ChatColor.YELLOW + " is ForceStarting the game...");
        else
            main.getGame().infoPlayers(ChatColor.GRAY + cs.getName() + ChatColor.YELLOW + " is ForceStarting the game...");
    }
}