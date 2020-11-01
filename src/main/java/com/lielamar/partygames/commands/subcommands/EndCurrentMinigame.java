package com.lielamar.partygames.commands.subcommands;

import com.lielamar.lielsutils.commands.Command;
import com.lielamar.partygames.Main;
import com.lielamar.partygames.game.GameState;
import com.lielamar.partygames.game.Minigame;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndCurrentMinigame extends Command {

    private Main main;

    public EndCurrentMinigame(Main main, String name) {
        super(name);

        this.main = main;
    }

    @Override
    public String getDescription() {
        return "Ends the current minigame";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "endcurrent", "endminigame", "endm" };
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "partygames.admin.endcurrentminigame" };
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

        Minigame minigame = main.getGame().getCurrentGame();
        if(minigame == null) {
            cs.sendMessage(ChatColor.RED + "The current minigame is NULL!");
            return;
        }

        minigame.stopMinigame();
        if(cs instanceof Player)
            main.getGame().infoPlayers(((Player)cs).getDisplayName() + ChatColor.YELLOW + " is ending the current minigame...");
        else
            main.getGame().infoPlayers(ChatColor.GRAY + cs.getName() + ChatColor.YELLOW + " is ending the current minigame...");
    }
}