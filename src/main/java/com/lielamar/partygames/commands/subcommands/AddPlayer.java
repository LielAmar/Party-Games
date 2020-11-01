package com.lielamar.partygames.commands.subcommands;

import com.lielamar.lielsutils.commands.Command;
import com.lielamar.partygames.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddPlayer extends Command {

    private Main main;

    public AddPlayer(Main main, String name) {
        super(name);

        this.main = main;
    }

    @Override
    public String getDescription() {
        return "Add a player to the game";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "add", "addp", "forceadd" };
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "partygames.admin.addplayer" };
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if(!hasPermissions(cs)) {
            cs.sendMessage(ChatColor.RED + "You don't have enough permissions to do that!");
            return;
        }

        if(args.length == 0) {
            cs.sendMessage(ChatColor.RED + "Please provide a player name!");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if(player == null) {
            cs.sendMessage(ChatColor.GRAY + args[0] + ChatColor.RED + " is not online/not a valid player!");
            return;
        }

        if(main.getGame().getPlayerIndex(player) != -1) {
            cs.sendMessage(ChatColor.GRAY + args[0] + ChatColor.RED + " is already in this game!");
            return;
        }

        main.getGame().addPlayer(player, true);
        main.getScoreboardManager().ejectPlayer(player);
        main.getScoreboardManager().injectPlayer(player);
        cs.sendMessage(player.getDisplayName() + ChatColor.GREEN + " has been forcefully added to the current running game!");
    }
}