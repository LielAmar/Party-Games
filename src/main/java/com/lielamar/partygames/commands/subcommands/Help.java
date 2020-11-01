package com.lielamar.partygames.commands.subcommands;

import com.lielamar.lielsutils.commands.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Set;

public class Help extends Command {

    private Set<Command> commands;

    public Help(String name, Set<Command> commands) {
        super(name);

        this.commands = commands;
    }

    @Override
    public String getDescription() {
        return "All of partygames admin commands";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "partygames.admin.help" };
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if(!hasPermissions(cs)) {
            cs.sendMessage(ChatColor.RED + "You don't have enough permissions to do that!");
            return;
        }

        cs.sendMessage(ChatColor.GRAY + "---- " + ChatColor.AQUA + "Party Games Help" + ChatColor.GRAY + " ----");
        for(Command cmd : this.commands) {
            if(cmd.hasPermissions(cs)) {
                cs.sendMessage(ChatColor.AQUA + "• " + cmd.getName() + ChatColor.GRAY + ": " + cmd.getDescription());
                cs.sendMessage(ChatColor.AQUA + "Aliases " + Arrays.toString(cmd.getAliases()));
            }
        }
        cs.sendMessage(ChatColor.GRAY + "---- --------------- ----");
    }
}