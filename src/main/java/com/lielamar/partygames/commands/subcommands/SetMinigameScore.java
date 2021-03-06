package com.lielamar.partygames.commands.subcommands;

import com.lielamar.lielsutils.commands.Command;
import com.lielamar.partygames.PartyGames;
import com.lielamar.partygames.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMinigameScore extends Command {

    private PartyGames main;

    public SetMinigameScore(PartyGames main, String name) {
        super(name);

        this.main = main;
    }

    @Override
    public String getDescription() {
        return "Sets a player's minigame score";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "minigamescore", "mgscore", "setmgscore" };
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "partygames.admin.setminigamescore" };
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

        if(args.length < 2) {
            cs.sendMessage(ChatColor.RED + "Please provide a player and an amount of score!");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if(player == null) {
            cs.sendMessage(ChatColor.GRAY + args[0] + ChatColor.RED + " is not online/not a valid player!");
            return;
        }

        int playerIndex = main.getGame().getPlayerIndex(player);
        if(playerIndex == -1) {
            cs.sendMessage(ChatColor.GRAY + args[0] + ChatColor.RED + " is not in the game!");
            return;
        }

        try {
            int scoreToSet = Integer.parseInt(args[1]);

            main.getGame().getPlayers()[playerIndex].setMinigameScore(scoreToSet);
            cs.sendMessage(ChatColor.GRAY + "" + scoreToSet + ChatColor.GREEN + " minigame score has been set for " + player.getDisplayName() + ChatColor.GREEN + "!");
        } catch(IllegalArgumentException e) {
            cs.sendMessage(ChatColor.GRAY + args[1] + ChatColor.RED + " is not a number!");
        }
    }
}