package com.lielamar.partygames.commands.subcommands;

import com.lielamar.lielsutils.commands.Command;
import com.lielamar.partygames.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemovePlayer extends Command {

    private Main main;

    public RemovePlayer(Main main, String name) {
        super(name);

        this.main = main;
    }

    @Override
    public String getDescription() {
        return "Remove a player from the game";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "remove", "removep", "kick" };
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "partygames.admin.removeplayer" };
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

        if(main.getGame().getPlayerIndex(player) == -1) {
            cs.sendMessage(ChatColor.GRAY + args[0] + ChatColor.RED + " is not in this game!");
            return;
        }

        main.getGame().removePlayer(player);
        cs.sendMessage(ChatColor.GRAY + args[0] + ChatColor.GREEN + " has been forcefully removed from the current running game!");
        turnToSpectator(player);
    }

    /**
     * Turns a player to a spectator if they have the partygames.spectate permission
     *
     * @param player   Player to turn to a spectator
     */
    public void turnToSpectator(Player player) {
        if(!player.hasPermission("partygames.spectate")) {
            player.kickPlayer(ChatColor.RED + "The game has already started and we couldn't add you to it!");
            return;
        }

        main.getScoreboardManager().getScoreboard(player).setScoreboard(main.getGame().getStaffScoreboard());
        player.setFlying(true);
        player.setAllowFlight(true);
        Bukkit.getOnlinePlayers().stream()
                .filter(pl ->!pl.hasPermission("partygames.seespectators") || main.getGame().containsPlayer(pl))
                .forEach(pl -> pl.hidePlayer(player));
    }
}
