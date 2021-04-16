package src.utilityclasses;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Lightweight class which manages the audio of the GUI and the game. All audio files are assumed to
 * be in the folder "sounds/".
 */
public class AudioPlayer {

    private Clip music_clip;
    private String music_currently_played = "";
    private AudioInputStream music_input_stream;

    private List<String> muted_sound_files = new LinkedList<>();
    private String file_path = "sounds/";

    /**
     * Plays music according to the string parameter of the method. Requested audio file won't be
     * played if the file_name is contained in the muted sounds. Audio file is looped forever.
     */
    public void play_music(String file_name)
        throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        if (!music_currently_played.equals(file_name)) {
            if (music_clip != null) {
                stop_music();
            }
            if (!muted_sound_files.contains(file_name)) {
                music_currently_played = file_name;
                ClassLoader cl = AudioPlayer.class.getClassLoader();
                URL url = cl.getResource(file_path + file_name);
                music_input_stream = AudioSystem.getAudioInputStream(Objects.requireNonNull(url));
                music_clip = AudioSystem.getClip();
                music_clip.open(music_input_stream);
                music_clip.loop(Clip.LOOP_CONTINUOUSLY);
                music_clip.start();
            }
        }
    }

    /**
     * Adds or deletes the string of a sound file from the muted_sound_files list depending on the
     * mute boolean.
     */
    public void mute_sound(String sound_file, boolean mute) {
        if (mute) {
            muted_sound_files.add(sound_file);
        } else {
            muted_sound_files.remove(sound_file);
        }
        if (sound_file.equals(music_currently_played)) {
            try {
                stop_music();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops the currently played music file. Note that the music can't be resumed. Once stopped the
     * sound file will be played from the beginning.
     */
    public void stop_music() throws IOException {
        if (music_clip == null) {
            return;
        }
        music_currently_played = "";
        music_clip.stop();
        music_clip.close();
        music_input_stream.close();
        music_clip = null;
    }

    /**
     * Plays a sound file once instead forever as in the method play_music(). Music can be played
     * concurrently.
     */
    public void play_sound(String file_name)
        throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        if (!muted_sound_files.contains(file_name)) {
            ClassLoader cl = AudioPlayer.class.getClassLoader();
            AudioInputStream input_stream = AudioSystem.getAudioInputStream(
                Objects.requireNonNull(cl.getResource(file_path + file_name)));
            Clip sound_clip = AudioSystem.getClip();
            sound_clip.open(input_stream);
            sound_clip.start();
            input_stream.close();
        }
    }
}