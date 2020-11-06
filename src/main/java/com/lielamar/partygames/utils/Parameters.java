package com.lielamar.partygames.utils;

import org.bukkit.configuration.file.FileConfiguration;

public class Parameters {

    // GENERAL
    private static String minigames_dir = "games/";

    // GAME
    private static boolean allow_duplicate_games = false;
    private static int countdown_time = 30;
    private static int minimum_players = 6;
    private static int maximum_players = 8;
    private static int amount_of_games = 8;

    // MINIGAME
    private static int minigame_start_time = 5;
    private static int minigame_end_time = 5;

    private Parameters() {}

    public static void initiate(FileConfiguration config) {
        if(config.contains("parameters.minigames_dir")) minigames_dir = config.getString("parameters.minigames_dir");

        if(config.contains("parameters.allow_duplicate_games")) allow_duplicate_games = config.getBoolean("parameters.allow_duplicate_games");
        if(config.contains("parameters.countdown_time")) countdown_time = config.getInt("parameters.countdown_time");
        if(config.contains("parameters.minimum_players")) minimum_players = config.getInt("parameters.minimum_players");
        if(config.contains("parameters.maximum_players")) maximum_players = config.getInt("parameters.maximum_players");
        if(config.contains("parameters.amount_of_games")) amount_of_games = config.getInt("parameters.amount_of_games");

        if(config.contains("parameters.minigame_start_time")) minigame_start_time = config.getInt("parameters.minigame_start_time");
        if(config.contains("parameters.minigame_end_time")) minigame_end_time = config.getInt("parameters.minigame_end_time");
    }

    public static String MINIGAMES_DIR() { return minigames_dir; }

    public static boolean ALLOW_DUPLICATE_GAMES() { return allow_duplicate_games; }
    public static int COUNTDOWN_TIME() { return countdown_time; }
    public static int MINIMUM_PLAYERS() { return minimum_players; }
    public static int MAXIMUM_PLAYERS() { return maximum_players; }
    public static int AMOUNT_OF_GAMES() { return amount_of_games; }

    public static int MINIGAME_START_TIME() { return minigame_start_time; }
    public static int MINIGAME_END_TIME() { return minigame_end_time; }
}
