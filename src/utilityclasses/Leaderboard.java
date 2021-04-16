package src.utilityclasses;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The leaderboard tracks stats of individual played game to display after a game has finished.
 * Keeps track of name, score, date, speed setting, level and game mode.
 */
public class Leaderboard {

    // Util
    private static final String LEADERBOARD_FILE = "lb.dat";
    /**
     * Leaderboard is stored as a list of serializable LeaderboardEntrys.
     */
    private List<LeaderboardEntry> all_entries = new ArrayList<>();
    private Comparator<LeaderboardEntry> score_comparator = (LeaderboardEntry entry1, LeaderboardEntry entry2) -> Integer
        .compare(entry2.getScore(), entry1.getScore());

    // Streams
    private ObjectOutputStream output_stream = null;
    private ObjectInputStream input_stream = null;

    /**
     * Loads the leaderboard returns it as a List of LeaderboardEntrys.
     */
    public List<LeaderboardEntry> get_lb_list() {
        load_file();
        all_entries.sort(score_comparator);
        return all_entries;
    }

    /**
     * Loads the leaderboard, adds an entry and saves the new file.
     */
    public void add_entry(String player_name, int game_score, String date, String speed_setting,
        String level_name, String game_mode) {
        load_file();
        all_entries.add(
            new LeaderboardEntry(player_name, game_score, date, speed_setting, level_name,
                game_mode));
        update_file();
    }

    /**
     * Uses Object Streams to load the leaderboard file 'lb.dat'.
     */
    private void load_file() {
        try {
            input_stream = new ObjectInputStream(new FileInputStream(LEADERBOARD_FILE));
            all_entries = (List<LeaderboardEntry>) input_stream.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("[Load] FNF Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[Load] IO Error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("[Load] CNF Error: " + e.getMessage());
        } finally {
            try {

                if (output_stream != null) {
                    output_stream.flush();
                    output_stream.close();
                }

            } catch (IOException e) {
                System.out.println("[Load] IO Error: " + e.getMessage());
            }
        }
    }

    /**
     * Uses Object Streams to store the leaderboard file to 'lb.dat'.
     */
    private void update_file() {
        try {

            output_stream = new ObjectOutputStream(new FileOutputStream(LEADERBOARD_FILE));
            output_stream.writeObject(all_entries);

        } catch (FileNotFoundException e) {
            System.out.println(
                "[Update] FNF Error: " + e.getMessage()
                    + ",the program will try and make a new file");
        } catch (IOException e) {
            System.out.println("[Update] IO Error: " + e.getMessage());
        } finally {
            try {

                if (output_stream != null) {
                    output_stream.flush();
                    output_stream.close();
                }

            } catch (IOException e) {
                System.out.println("[Update] Error: " + e.getMessage());
            }
        }
    }
}