package src.utilityclasses;

import java.io.Serializable;

/**
 * LeaderboardEntry stores details of a played game.
 */
public class LeaderboardEntry implements Serializable {

    // Stats
    private String player_name;
    private int game_score;
    private String date;
    private String speed_setting;
    private String level_name;
    private String game_mode;

    public LeaderboardEntry(String player_name, int game_score, String date, String speed_setting,
        String level_name, String game_mode) {
        this.player_name = player_name;
        this.game_score = game_score;
        this.date = date;
        this.game_mode = game_mode;
        this.speed_setting = speed_setting;
        this.level_name = level_name;
    }

    /**
     * Returns all stats with a placeholder for the ranking.
     */
    public String[] getAll() {
        return new String[]{"Ranking Placeholder", player_name, String.valueOf(game_score), date,
            speed_setting, level_name, game_mode};
    }

    /**
     * Returns score of the entry.
     */
    public int getScore() {
        return game_score;
    }
}
