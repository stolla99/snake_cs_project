package src.utilityclasses;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import src.gameclasses.Level;

/**
 * Object to store and load level data to 'levels.dat'.
 */
public class LevelSerializer {

    private static final String LEVEL_FILE = "levels.dat";
    /**
     * Map of level name to Level Object.
     */
    private static Map<String, Level> all_levels = new HashMap<>();
    private static ObjectOutputStream output_stream = null;

    /**
     * Returns all levels that fit given size requirements as a Map of level name to Level Object.
     */
    public static Map<String, Level> get_all_levels(int num_rect_x, int num_rect_y) {
        load_file();
        Map<String, Level> filtered_levels = new HashMap<>();
        all_levels.entrySet().parallelStream().forEach(stringLevelEntry -> {
            Level level = stringLevelEntry.getValue();
            if (level.get_width() == num_rect_x && level.get_height() == num_rect_y) {
                filtered_levels.put(stringLevelEntry.getKey(), stringLevelEntry.getValue());
            }
        });
        return filtered_levels;
    }

    /**
     * Returns all levels as a Map of level name to Level Object.
     */
    public static Map<String, Level> get_all_levels() {
        load_file();
        return all_levels;
    }

    /**
     * Loads the level data file, adds a level and saves the new file.
     */
    public static void add_gameBoard(String identifier, Integer[][] gameBoard, Integer[] head,
        Integer[] tail, Boolean modifiable, Integer direction, String name) {
        load_file();
        all_levels.put(identifier, new Level(gameBoard, head, tail, modifiable, direction, name));
        update_file();
    }

    /**
     * Loads the level data file, removes a level and saves the new file.
     */
    public static void remove_gameBoard(String identifier) {
        load_file();
        all_levels.remove(identifier);
        update_file();
    }

    /**
     * Uses Object Streams to load the level data file 'levels.dat'.
     */
    private static void load_file() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(LEVEL_FILE));
            all_levels = (Map<String, Level>) inputStream.readObject();
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
     * Uses Object Streams to store the level data file to 'levels.dat'.
     */
    private static void update_file() {
        try {
            output_stream = new ObjectOutputStream(new FileOutputStream(LEVEL_FILE));
            output_stream.writeObject(all_levels);
        } catch (FileNotFoundException e) {
            System.out.println("[Update] FNF Error (program will create one): " + e.getMessage());
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