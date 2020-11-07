package com.lielamar.partygames.game;

import com.lielamar.partygames.utils.ScoreboardType;

public enum GameType {

    ANIMAL_SLAUGHTER("Animal Slaughter", "Get points by killing animals!", 45, ScoreboardType.BY_MINIGAME_SCORE),
    ANVIL_SPLEEF("Anvil Spleef", "Avoid the falling anvils!", 120, ScoreboardType.BY_GAME_SCORE),
    AVALANCHE("Avalanche", "Avoid the snowballs!", 5, ScoreboardType.BY_GAME_SCORE), // Game Duration here represents the duration of each "wave"
    BOMBARDMENT("Bombardment", "Avoid the cannon balls!", 180, ScoreboardType.BY_MINIGAME_SCORE),
    CANNON_PAINTERS("Cannon Painters", "Paint the most wool in your color!", 30, ScoreboardType.BY_MINIGAME_SCORE),
    CHICKEN_RINGS("Chicken Rings", "Be the first at the finish line!", 240, ScoreboardType.BY_MINIGAME_SCORE),
    DIVE("Dive", "Dive down and cover the most space!", 60, ScoreboardType.BY_MINIGAME_SCORE),
    FIRE_LEAPERS("Fire Leapers", "Avoid the fire leapers!", 180, ScoreboardType.BY_MINIGAME_SCORE),
    FROZEN_FLOOR("Frozen Floor", "Be the last player on the frozen floor!", 120, ScoreboardType.BY_GAME_SCORE),
    HIGH_GROUND("High Ground", "Knock players away and have the high ground!", 75, ScoreboardType.BY_MINIGAME_SCORE),
    HOE_HOE_HOE("Hoe Hoe Hoe", "Claim as much land as possible with your hoe!", 60, ScoreboardType.BY_MINIGAME_SCORE),
    JIGSAW_RUSH("Jigsaw Rush", "Copy the big canvas into your small one!", 90, ScoreboardType.BY_MINIGAME_SCORE),
    JUNGLE_JUMP("Jungle Jump", "Be the first player at the finish line!", 90, ScoreboardType.BY_MINIGAME_SCORE),
    LAB_ESCAPE("Lab Escape", "Escape the laboratory using your tools!", 180, ScoreboardType.BY_MINIGAME_SCORE),
    LAWN_MOOWER("Lawn Moower", "Feed your cow the most grass!", 60, ScoreboardType.BY_MINIGAME_SCORE),
    MINECART_RACING("Minecart Racing", "Shoot the wool in your color to boost your minecart!", 120, ScoreboardType.BY_MINIGAME_SCORE),
    PIG_FISHING("Pig Fishing", "Fish the most pigs into your hole!",  90, ScoreboardType.BY_MINIGAME_SCORE),
    PIG_JOUSTING("Pig Jousting", "Be the last player alive!", 120, ScoreboardType.BY_GAME_SCORE),
    RPG_16("RPG-16", "Kill the most players using your RPG-16!", 90, ScoreboardType.BY_MINIGAME_SCORE),
    SHOOTING_RANGE("Shooting Range", "Kill the most mobs with your bow!", 60, ScoreboardType.BY_MINIGAME_SCORE),
    SPIDER_MAZE("Spider Maze", "Be the first one to finish the maze!", 120, ScoreboardType.BY_GAME_SCORE),
    SUPER_SHEEP("Super Sheep", "Avoid touching other players' trail!", 120, ScoreboardType.BY_GAME_SCORE),
    THE_FLOOR_IS_LAVA("The Floor Is Lava", "Win by having a good rhythm!", 90, ScoreboardType.BY_MINIGAME_SCORE),
    TRAMPOLINIO("Trampolinio", "Catch the most scores in the Trampoline!", 120, ScoreboardType.BY_MINIGAME_SCORE),
    VOLCANO("Volcano", "Watch your steps and be the last one alive!", 180, ScoreboardType.BY_GAME_SCORE),
    WORKSHOP("Workshop", "Craft the Villager's desired item as fast as possible!", 240, ScoreboardType.BY_MINIGAME_SCORE);

    private String name;
    private String description;
    private int gameDuration;
    private ScoreboardType scoreboardType;

    GameType(String name, String description, int gameDuration, ScoreboardType scoreboardType) {
        this.name = name;
        this.description = description;
        this.gameDuration = gameDuration;
        this.scoreboardType = scoreboardType;
    }

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public int getGameDuration() { return this.gameDuration; }
    public ScoreboardType getScoreboardType() { return this.scoreboardType; }
}
