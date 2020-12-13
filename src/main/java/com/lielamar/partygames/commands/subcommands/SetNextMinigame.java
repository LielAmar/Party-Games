package com.lielamar.partygames.commands.subcommands;

import com.lielamar.lielsutils.commands.Command;
import com.lielamar.partygames.PartyGames;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.GameType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SetNextMinigame extends Command {

    private PartyGames main;

    public SetNextMinigame(PartyGames main, String name) {
        super(name);

        this.main = main;
    }

    @Override
    public String getDescription() {
        return "Sets the next minigame";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "setnext", "next" };
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "partygames.admin.setnextminigame" };
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

        if(main.getGame().getCurrentGameId() >= main.getGame().getMinigames().length) {
            cs.sendMessage(ChatColor.RED + "This minigame is the last one!");
            return;
        }

        if(args.length == 0) {
            cs.sendMessage(ChatColor.RED + "Please provide a game name to set next!");
            return;
        }

        String nextMinigameName = fixName(args[0]);
        try {
            main.getGame().getMinigames()[main.getGame().getCurrentGameId()] = GameType.valueOf(nextMinigameName);
            cs.sendMessage(ChatColor.GRAY + nextMinigameName + ChatColor.GREEN + " will be the next minigame!");
        } catch(IllegalArgumentException e) {
            cs.sendMessage(ChatColor.GRAY + nextMinigameName + ChatColor.RED + " is not a valid minigame!");
        }
    }

    /**
     * Changes the name received to the ENUM convention (xxx xxx -> XXX_XXX)
     *
     * @param name   Name to change
     * @return       Enum convention
     */
    private String fixName(String name) {
        return name.toUpperCase().replaceAll(" ", "_");
    }
}