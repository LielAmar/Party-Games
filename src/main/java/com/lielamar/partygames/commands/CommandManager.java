package com.lielamar.partygames.commands;

import com.lielamar.lielsutils.commands.Command;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.commands.subcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandManager implements CommandExecutor, TabCompleter {

    private Main main;

    private final Set<Command> commands;

    private final String mainCommand = "partygames";
    private final String addplayerCommand = "addplayer";
    private final String removeplayerCommand = "removeplayer";
    private final String forcestartCommand = "forcestart";
    private final String endgameCommand = "endgame";
    private final String setnextminigameCommand = "setnextminigame";
    private final String endcurrentminigameCommand = "endcurrentminigame";
    private final String setscoreCommand = "setscore";
    private final String setminigamescoreCommand = "setminigamescore";
    private final String helpCommand = "help";

    public CommandManager(Main main) {
        this.main = main;
        main.getCommand(mainCommand).setTabCompleter(this);
        main.getCommand(mainCommand).setExecutor(this);

        this.commands = new HashSet<>();
        this.setupCommands();
    }

    /**
     * Sets up all of the sub commands to /PartyGames
     */
    public void setupCommands() {
        commands.add(new AddPlayer(main, addplayerCommand));
        commands.add(new RemovePlayer(main, removeplayerCommand));
        commands.add(new ForceStart(main, forcestartCommand));
        commands.add(new EndGame(main, endgameCommand));
        commands.add(new SetNextMinigame(main, setnextminigameCommand));
        commands.add(new EndCurrentMinigame(main, endcurrentminigameCommand));
        commands.add(new SetScore(main, setscoreCommand));
        commands.add(new SetMinigameScore(main, setminigamescoreCommand));
        commands.add(new Help(helpCommand, this.commands));
    }

    /**
     * Returns a {@link com.lielamar.lielsutils.commands.Command} object related to the given name
     *
     * @param name   Name/Alias of the command to return
     * @return       The command object from the commands set
     */
    public Command getCommand(String name) {
        for(Command cmd : this.commands) {
            if(cmd.getName().equalsIgnoreCase(name))
                return cmd;

            for(String s : cmd.getAliases()) {
                if(s.equalsIgnoreCase(name))
                    return cmd;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender cs, org.bukkit.command.Command cmd, String cmdLabel, String[] args) {
        if(cmd.getName().equalsIgnoreCase(mainCommand)) {
            if(!cs.hasPermission("partygames.admin")) {
                cs.sendMessage(ChatColor.RED + "You don't have enough permissions to do that!");
                return false;
            }

            if(args.length == 0) {
                getCommand("help").execute(cs, null);
                return false;
            }

            Command command = getCommand(args[0]);

            if(command == null) {
                getCommand("help").execute(cs, null);
                return false;
            }

            String[] newArguments = new String[args.length - 1];
            System.arraycopy(args, 1, newArguments, 0, newArguments.length);

            command.execute(cs, newArguments);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, org.bukkit.command.Command cmd, String cmdLabel, String[] args) {
        if(cmd.getName().equalsIgnoreCase(mainCommand)) {
            if(!cs.hasPermission("partygames.admin"))
                return null;

            if(args.length == 0) {
                List<String> tabComplete = new ArrayList<>();
                this.commands.forEach(subCmd -> tabComplete.add(subCmd.getName()));
                return tabComplete;
            }
        }
        return null;
    }
}