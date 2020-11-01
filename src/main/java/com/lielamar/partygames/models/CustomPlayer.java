package com.lielamar.partygames.models;

import com.lielamar.lielsutils.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class CustomPlayer {

    private Player player;
    private Scoreboard assignedScoreboard;

    private int totalScore;
    private int minigameScore;

    private boolean spectator;

    private BossBar bossBar;

    public CustomPlayer(Player player) {
        this.player = player;
        this.assignedScoreboard = player.getScoreboard();

        this.totalScore = 0;
        this.minigameScore = 0;

        this.spectator = false;
    }

    public Player getPlayer() { return this.player; }
    public void setAssignedScoreboard(Scoreboard sb) { this.assignedScoreboard = sb; }
    public Scoreboard getAssignedScoreboard() { return this.assignedScoreboard; }

    public void addScore(int score) {
        this.totalScore+=score;
    }
    public void takePoints(int score) { this.totalScore-=score; }
    public void setScore(int score) {
        this.totalScore=score;
    }
    public void resetScore() {
        this.totalScore=0;
    }
    public int getScore() { return this.totalScore; }

    public void addMinigameScore(int score) {
        this.minigameScore+=score;
    }
    public void takeMinigamePoints(int score) { this.minigameScore-=score; }
    public void setMinigameScore(int score) {
        this.minigameScore=score;
    }
    public void resetMinigameScore() {
        this.minigameScore=0;
    }
    public int getMinigameScore() { return this.minigameScore; }

    public boolean isSpectator() { return this.spectator; }
    public void setSpectator(boolean choice) { this.spectator = choice; }

    public void updateBossBar(String message) {
        if(bossBar == null)
            bossBar = new BossBar(message, player.getLocation());
        else
            bossBar.update(message, player.getLocation());

        bossBar.display(player);
    }

    public void destroyBossBar() {
        if(this.bossBar == null) return;
        this.bossBar.destroy(this.player);
        this.bossBar = null;
    }
}
