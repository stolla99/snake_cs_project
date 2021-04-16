package src.utilityclasses;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import src.gameclasses.Settings;

/**
 * Object to store and load settings to 'settings.dat'.
 */
public class SettingsSerializer {

    private static final String LEVEL_FILE = "settings.dat";
    private static ObjectOutputStream output_stream = null;

    /**
     * Uses Object Streams to load the settings file 'settings.dat'.
     */
    public static Settings load_file() {
        Settings settings = null;
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(LEVEL_FILE));
            settings = (Settings) inputStream.readObject();
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
        return settings;
    }

    /**
     * Uses Object Streams to store the settings file to 'settings.dat'.
     */
    public static void update_file(Settings settings) {
        try {
            output_stream = new ObjectOutputStream(new FileOutputStream(LEVEL_FILE));
            output_stream.writeObject(settings);
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